package org.darts.dartsmanagement.data.collections

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.darts.dartsmanagement.data.collections.response.CollectionFirestoreResponse
import org.darts.dartsmanagement.data.common.firestore.BarFirestoreResponse
import org.darts.dartsmanagement.data.common.firestore.MachineFirestoreResponse
import org.darts.dartsmanagement.data.auth.SessionManager
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.data.collections.toDomain // Import the extension function

@Serializable
data class MonthStatus(
    val licenseId: String = "",
    val status: String = ""
)

// Extension function to convert kotlinx.datetime.Instant to dev.gitlive.firebase.firestore.Timestamp
fun Instant.toFirebaseTimestamp(): Timestamp {
    return Timestamp(this.epochSeconds, this.nanosecondsOfSecond)
}

@Serializable
data class CollectionMachineFirestore(
    val machineId: String,
    val barAmount: Double,
    val businessAmount: Double,
    val totalCollection: Double
)

@Serializable
data class CollectionSaveRequest(
    val licenseId: String,
    val status: String,
    val barId: String,
    val barName: String,
    val totalBarAmount: Double,
    val totalBusinessAmount: Double,
    val totalCollection: Double,
    val comments: String,
    val createdAt: Timestamp,
    val billingMonth: String,
    val recordedBy: String,
    val machinesCollection: List<CollectionMachineFirestore>
)

class CollectionsApiService(
    private val firestore: ExpectedFirestore,
    private val sessionManager: SessionManager
) {

    suspend fun saveCollection(
        barId: String,
        barName: String,
        comments: String,
        totalBarAmount: Double,
        totalBusinessAmount: Double,
        totalCollection: Double,
        machines: List<CollectionMachineFirestore>,
        machineCounters: Map<String, Int> // machineId to new counter
    ): String? {
        return try {
            val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("License not found in session")
            val userId = sessionManager.userId.value ?: throw IllegalStateException("User ID not found in session")
            val billingMonth = findOpenBillingMonth(licenseId) ?: throw IllegalStateException("No open billing month found for license $licenseId")

            val collectionMap = mapOf<String, Any?>(
                "licenseId" to licenseId,
                "status" to "ACTIVE",
                "barId" to barId,
                "barName" to barName,
                "totalBarAmount" to totalBarAmount,
                "totalBusinessAmount" to totalBusinessAmount,
                "totalCollection" to totalCollection,
                "comments" to comments,
                "createdAt" to Timestamp.now(),
                "billingMonth" to billingMonth,
                "recordedBy" to userId,
                "machinesCollection" to machines.map {
                    mapOf(
                        "machineId" to it.machineId,
                        "barAmount" to it.barAmount,
                        "businessAmount" to it.businessAmount,
                        "totalCollection" to it.totalCollection
                    )
                }
            )

            val collectionId = firestore.addDocument("collections", collectionMap)
            
            // Update each machine's counter
            machineCounters.forEach { (mId, newCounter) ->
                firestore.updateDocumentFields(
                    "machines",
                    mId,
                    mapOf("counter" to newCounter)
                )
            }
            collectionId
        } catch (e: Exception) {
            println("Error saving collection: $e")
            null
        }
    }

    private suspend fun findOpenBillingMonth(licenseId: String): String? {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        // Try current month
        val currentMonth = "${now.year}-${now.monthNumber.toString().padStart(2, '0')}"
        val currentId = "${licenseId}_$currentMonth"
        val currentDoc = firestore.getDocument("monthStatus", currentId)
        if (currentDoc != null && currentDoc.data<MonthStatus>().status == "open") {
            return currentMonth
        }

        // Try previous month
        val prevMonthDate = now.date.minus(1, DateTimeUnit.MONTH)
        val prevMonth = "${prevMonthDate.year}-${prevMonthDate.monthNumber.toString().padStart(2, '0')}"
        val prevId = "${licenseId}_$prevMonth"
        val prevDoc = firestore.getDocument("monthStatus", prevId)
        if (prevDoc != null && prevDoc.data<MonthStatus>().status == "open") {
            return prevMonth
        }

        // Fallback: query any open status for this license
        val queryResult = firestore.getDocumentsQuery("monthStatus")
            .whereEqualTo("licenseId", licenseId)
            .whereEqualTo("status", "open")
            .get()

        if (queryResult.documents.isNotEmpty()) {
            // Pick the one from the ID if possible, or just the first one
            val doc = queryResult.documents.first()
            return doc.id.substringAfterLast("_")
        }

        return null
    }

    suspend fun deleteCollection(collectionId: String) {
        firestore.deleteDocument("collections", collectionId)
    }

    suspend fun updateCollection(
        collectionId: String,
        comments: String,
        totalBarAmount: Double,
        totalBusinessAmount: Double,
        totalCollection: Double,
        machines: List<CollectionMachineFirestore>
    ) {
        val updateMap = mapOf<String, Any?>(
            "comments" to comments,
            "totalBarAmount" to totalBarAmount,
            "totalBusinessAmount" to totalBusinessAmount,
            "totalCollection" to totalCollection,
            "machinesCollection" to machines.map {
                mapOf(
                    "machineId" to it.machineId,
                    "barAmount" to it.barAmount,
                    "businessAmount" to it.businessAmount,
                    "totalCollection" to it.totalCollection
                )
            },
            "modifiedOn" to Timestamp.now()
        )
        firestore.updateDocument("collections", collectionId, updateMap)
    }

    private suspend fun getFullCollectionModels(documentSnapshots: List<dev.gitlive.firebase.firestore.DocumentSnapshot>): List<CollectionModel> {
        val collectionFirestoreResponsesWithIds = documentSnapshots.map {
            it.id to it.data<CollectionFirestoreResponse>()
        }

        // Fetch all bars once (filtered by license)
        val licenseId = sessionManager.licenseId.value ?: return emptyList()
        val allBarsDocs = firestore.getDocuments("bars", "licenseId", licenseId)
        val allBars = allBarsDocs.mapNotNull { doc ->
            doc.data<BarFirestoreResponse>().copy(id = doc.id)
        }

        val barIdToBarNameMap = allBars.associate { it.id.toString() to it.name }

        return collectionFirestoreResponsesWithIds.map { (collectionId, collectionResponse) ->
            val barId = collectionResponse.barId
            val barName = barIdToBarNameMap[barId] ?: collectionResponse.barName.ifBlank { "Unknown Bar" }

            // Use the toDomain extension function
            collectionResponse.toDomain(collectionId, barName)
        }
    }


    suspend fun getCollectionsByMachineId(
        machineId: String
    ): List<CollectionModel> {
        return try {
            val licenseId = sessionManager.licenseId.value ?: return emptyList()
            // We search for documents where machinesCollection contains a map with machineId
            // Note: Firestore simplified abstraction might not support array-contains for maps easily, 
            // so we might need to fetch all and filter locally if getDocuments doesn't support it.
            // For now, let's fetch all collections of the license and filter.
            val snapshot = firestore.getDocuments("collections", "licenseId", licenseId)
            val filteredSnapshot = snapshot.filter { doc ->
                val response = doc.data<CollectionFirestoreResponse>()
                response.machinesCollection.any { it.machineId == machineId }
            }
            getFullCollectionModels(filteredSnapshot)
        } catch (e: Exception) {
            println("Error getting collections by machine id: $e")
            emptyList()
        }
    }

    suspend fun getCollectionsForMonth(year: Int, month: Int): List<CollectionModel> {
        return try {
            val licenseId = sessionManager.licenseId.value ?: return emptyList()
            val billingMonth = "${year}-${month.toString().padStart(2, '0')}"
            
            val snapshot = firestore.getDocumentsQuery("collections")
                .whereEqualTo("licenseId", licenseId)
                .whereEqualTo("billingMonth", billingMonth)
                .get()

            getFullCollectionModels(snapshot.documents)
        } catch (e: Exception) {
            println("Error getting collections for month: $e")
            emptyList()
        }
    }

    suspend fun getCollectionsInDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CollectionModel> {
        return try {
            val licenseId = sessionManager.licenseId.value ?: return emptyList()
            val startMillis =
                startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val endOfRangeExclusiveLocalDate = endDate.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
            val endMillis =
                endOfRangeExclusiveLocalDate.atStartOfDayIn(TimeZone.currentSystemDefault())
                    .toEpochMilliseconds()

            val snapshot = firestore.getDocuments("collections", "licenseId", licenseId)

            val filteredSnapshot = snapshot.filter { doc ->
                val collection = doc.data<CollectionFirestoreResponse>()
                val createdAtMillis = (collection.createdAt?.seconds ?: 0) * 1000L
                createdAtMillis in startMillis until endMillis
            }
            getFullCollectionModels(filteredSnapshot)
        } catch (e: Exception) {
            println("Error getting collections in date range: $e")
            emptyList()
        }
    }

    suspend fun getPaginatedCollections(
        lastCollectionCreatedAtLong: Long?,
        lastCollectionDocumentId: String?,
        limit: Int
    ): List<CollectionModel> {
        return try {
            val licenseId = sessionManager.licenseId.value ?: return emptyList()
            var query = firestore.getDocumentsQuery("collections")
                .whereEqualTo("licenseId", licenseId)
                .orderBy("createdAt.seconds", ExpectedFirestore.Direction.DESCENDING)
                .orderBy("__name__", ExpectedFirestore.Direction.DESCENDING)
                .limit(limit)

            if (lastCollectionCreatedAtLong != null && lastCollectionDocumentId != null) {
                query = query.startAfter(
                    Timestamp(lastCollectionCreatedAtLong / 1000, 0),
                    
                    lastCollectionDocumentId
                )
            }

            val snapshot = query.get()
            getFullCollectionModels(snapshot.documents)
        } catch (e: Exception) {
            println("Error getting paginated collections: $e")
            emptyList()
        }
    }
}
