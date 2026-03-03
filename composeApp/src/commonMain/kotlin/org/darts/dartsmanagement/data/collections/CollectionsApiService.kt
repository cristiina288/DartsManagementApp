package org.darts.dartsmanagement.data.collections

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
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

// Extension function to convert kotlinx.datetime.Instant to dev.gitlive.firebase.firestore.Timestamp
fun Instant.toFirebaseTimestamp(): Timestamp {
    return Timestamp(this.epochSeconds, this.nanosecondsOfSecond)
}

@Serializable
data class CollectionSaveRequest(
    val machineId: Int,
    val comments: String,
    val totalCollection: Double,
    val barAmount: Double,
    val barPayment: Double,
    val businessAmount: Double,
    val extraAmount: Double,
    val createdAt: Timestamp,
    val status: Map<String, Int>? = null,
    val license_id: String = "",
    val userId: String? = null
)

class CollectionsApiService(
    private val firestore: ExpectedFirestore,
    private val sessionManager: SessionManager
) {

    suspend fun saveCollection(
        collectionAmountsModel: CollectionAmountsModel,
        newCounterMachine: Int,
        machineId: Int,
        barId: String,
        comments: String,
        groupId: String,
        leaguePayment: Map<String, Any>?
    ): Boolean {
        return try {
            val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("License not found in session")
            val userId = sessionManager.userId.value ?: throw IllegalStateException("User ID not found in session")
            val collectionMap = mutableMapOf<String, Any?>(
                "machineId" to machineId,
                "barId" to barId,
                "batchId" to groupId,
                "userId" to userId,
                "comments" to comments,
                "totalCollection" to collectionAmountsModel.totalCollection,
                "barAmount" to collectionAmountsModel.barAmount,
                "barPayment" to collectionAmountsModel.barPayment,
                "businessAmount" to collectionAmountsModel.businessAmount,
                "extraAmount" to collectionAmountsModel.extraAmount,
                "license_id" to licenseId,
                "createdAt" to Timestamp.now(),
                "status" to null
            )

            leaguePayment?.let {
                collectionMap["league_payment"] = it
            }

            firestore.addDocument("collections", collectionMap)
            firestore.updateDocumentFields(
                "machines",
                machineId.toString(),
                mapOf("counter" to newCounterMachine)
            )
            true
        } catch (e: Exception) {
            println("Error saving collection: $e")
            false
        }
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
