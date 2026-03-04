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
    val license_id: String = "",
    val status: String = ""
)

// Extension function to convert kotlinx.datetime.Instant to dev.gitlive.firebase.firestore.Timestamp
fun Instant.toFirebaseTimestamp(): Timestamp {
    return Timestamp(this.epochSeconds, this.nanosecondsOfSecond)
}

@Serializable
data class CollectionMachineFirestore(
    val machineId: Int,
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
        machines: List<CollectionMachineFirestore>,
        machineCounters: Map<String, Int> // machineId to new counter
    ): Boolean {
        return try {
            val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("License not found in session")
            val userId = sessionManager.userId.value ?: throw IllegalStateException("User ID not found in session")
            val billingMonth = findOpenBillingMonth(licenseId) ?: throw IllegalStateException("No open billing month found for license $licenseId")

            val collectionMap = mapOf<String, Any?>(
                "licenseId" to licenseId,
                "status" to "active",
                "barId" to barId,
                "barName" to barName,
                "totalBarAmount" to totalBarAmount,
                "totalBusinessAmount" to totalBusinessAmount,
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

            firestore.addDocument("collections", collectionMap)
            
            // Update each machine's counter
            machineCounters.forEach { (mId, newCounter) ->
                firestore.updateDocumentFields(
                    "machines",
                    mId,
                    mapOf("counter" to newCounter)
                )
            }
            true
        } catch (e: Exception) {
            println("Error saving collection: $e")
            false
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
            .whereEqualTo("license_id", licenseId)
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
        collectionAmountsModel: CollectionAmountsModel,
        comments: String
    ) {
        val updateMap = mapOf<String, Any?>(
            "comments" to comments,
            "totalCollection" to collectionAmountsModel.totalCollection,
            "barAmount" to collectionAmountsModel.barAmount,
            "barPayment" to collectionAmountsModel.barPayment,
            "businessAmount" to collectionAmountsModel.businessAmount,
            "extraAmount" to collectionAmountsModel.extraAmount,
            "modified_on" to Timestamp.now()
        )
        firestore.updateDocument("collections", collectionId, updateMap)
    }

    private suspend fun getFullCollectionModels(documentSnapshots: List<dev.gitlive.firebase.firestore.DocumentSnapshot>): List<CollectionModel> {
        val collectionFirestoreResponsesWithIds = documentSnapshots.map {
            it.id to it.data<CollectionFirestoreResponse>()
        }

        val machineIds = collectionFirestoreResponsesWithIds.map { it.second.machineId }.distinct()

        // Fetch all machines once (filtered by license)
        val licenseId = sessionManager.licenseId.value ?: return emptyList()
        val allMachinesDocs = firestore.getDocuments("machines", "license_id", licenseId)
        val allMachines = allMachinesDocs.mapNotNull { doc ->
            doc.data<MachineFirestoreResponse>().copy(id = doc.id)
        }

        // Filter machines based on machineIds from collections
        val machines = allMachines.filter { machine -> machineIds.contains(machine.id.toInt()) }
        val machineIdToBarIdMap = machines.associate { it.id.toInt() to (it.barId ?: "") }

        val barIds = collectionFirestoreResponsesWithIds.map { it.second.barId }.distinct()
            .filter { it.isNotBlank() }

        // Fetch all bars once (filtered by license)
        val allBarsDocs = firestore.getDocuments("bars", "license_id", licenseId)
        val allBars = allBarsDocs.mapNotNull { doc ->
            doc.data<BarFirestoreResponse>().copy(id = doc.id)
        }

        // Filter bars based on barIds from collections
        val bars = allBars.filter { bar -> barIds.contains(bar.id) }
        val barIdToBarNameMap = bars.associate { it.id.toString() to it.name }

        return collectionFirestoreResponsesWithIds.map { (collectionId, collectionResponse) ->
            val barId = collectionResponse.barId
            val barName = barIdToBarNameMap[barId] ?: "Unknown Bar"

            // Use the toDomain extension function
            collectionResponse.toDomain(collectionId, barName)
        }
    }


    suspend fun getCollectionsByMachineId(
        machineId: Int
    ): List<CollectionModel> {
        return try {
            val licenseId = sessionManager.licenseId.value ?: return emptyList()
            // Abstraction only supports one where clause for now, so we filter by machineId and check licenseId manually for safety
            val snapshot = firestore.getDocuments("collections", "machineId", machineId)
            val filteredSnapshot = snapshot.filter { it.data<CollectionFirestoreResponse>().license_id == licenseId }
            getFullCollectionModels(filteredSnapshot)
        } catch (e: Exception) {
            println("Error getting collections by machine id: $e")
            emptyList()
        }
    }

    suspend fun getCollectionsForMonth(year: Int, month: Int): List<CollectionModel> {
        return try {
            val licenseId = sessionManager.licenseId.value ?: return emptyList()
            val startOfMonth =
                LocalDate(year, month, 1).atStartOfDayIn(TimeZone.currentSystemDefault())
            val nextMonth =
                if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
            val startOfNextMonth = nextMonth.atStartOfDayIn(TimeZone.currentSystemDefault())

            val startMillis = startOfMonth.toEpochMilliseconds()
            val endMillis = startOfNextMonth.toEpochMilliseconds()
            val snapshot = firestore.getDocuments("collections", "license_id", licenseId)

            val filteredSnapshot = snapshot.filter { doc ->
                val collection = doc.data<CollectionFirestoreResponse>()
                val createdAtMillis = (collection.createdAt?.seconds ?: 0) * 1000L
                createdAtMillis in startMillis until endMillis
            }
            getFullCollectionModels(filteredSnapshot)
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

            val snapshot = firestore.getDocuments("collections", "license_id", licenseId)

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
                .whereEqualTo("license_id", licenseId)
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
