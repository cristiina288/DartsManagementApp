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
    val status: Map<String, Int>? = null
)

class CollectionsApiService(
    private val firestore: ExpectedFirestore
) {

    suspend fun saveCollection(
        collectionAmountsModel: CollectionAmountsModel,
        newCounterMachine: Int,
        machineId: Int,
        barId: String,
        comments: String
    ): Boolean {
        return try {
            val collectionMap = mapOf<String, Any?>(
                "machineId" to machineId,
                "barId" to barId,
                "comments" to comments,
                "totalCollection" to collectionAmountsModel.totalCollection,
                "barAmount" to collectionAmountsModel.barAmount,
                "barPayment" to collectionAmountsModel.barPayment,
                "businessAmount" to collectionAmountsModel.businessAmount,
                "extraAmount" to collectionAmountsModel.extraAmount,
                "createdAt" to Timestamp.now(),
                "status" to null
            )

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

    private suspend fun getFullCollectionModels(documentSnapshots: List<dev.gitlive.firebase.firestore.DocumentSnapshot>): List<CollectionModel> {
        val collectionFirestoreResponsesWithIds = documentSnapshots.map {
            it.id to it.data<CollectionFirestoreResponse>()
        }

        val machineIds = collectionFirestoreResponsesWithIds.map { it.second.machineId }.distinct()

        // Fetch all machines once
        val allMachinesDocs = firestore.getDocuments("machines")
        val allMachines = allMachinesDocs.mapNotNull { doc ->
            doc.data<MachineFirestoreResponse>().copy(id = doc.id)
        }

        // Filter machines based on machineIds from collections
        val machines = allMachines.filter { machine -> machineIds.contains(machine.id.toInt()) }
        val machineIdToBarIdMap = machines.associate { it.id.toInt() to (it.barId ?: "") }

        val barIds = collectionFirestoreResponsesWithIds.map { it.second.barId }.distinct()
            .filter { it.isNotBlank() }

        // Fetch all bars once
        val allBarsDocs = firestore.getDocuments("bars")
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
            val snapshot = firestore.getDocuments("collections", "machineId", machineId)
            getFullCollectionModels(snapshot)
        } catch (e: Exception) {
            println("Error getting collections by machine id: $e")
            emptyList()
        }
    }

    suspend fun getCollectionsForMonth(year: Int, month: Int): List<CollectionModel> {
        return try {
            val startOfMonth =
                LocalDate(year, month, 1).atStartOfDayIn(TimeZone.currentSystemDefault())
            val nextMonth =
                if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
            val startOfNextMonth = nextMonth.atStartOfDayIn(TimeZone.currentSystemDefault())

            val startMillis = startOfMonth.toEpochMilliseconds()
            val endMillis = startOfNextMonth.toEpochMilliseconds()
            val snapshot = firestore.getDocuments("collections")

            val filteredSnapshot = snapshot.filter { doc ->
                val collection = doc.data<CollectionFirestoreResponse>()
                val createdAtMillis = collection.createdAt?.seconds?.times(1000L)
                createdAtMillis != null && createdAtMillis in startMillis until endMillis
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
            val startMillis =
                startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val endOfRangeExclusiveLocalDate = endDate.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
            val endMillis =
                endOfRangeExclusiveLocalDate.atStartOfDayIn(TimeZone.currentSystemDefault())
                    .toEpochMilliseconds()

            val snapshot = firestore.getDocuments("collections")

            val filteredSnapshot = snapshot.filter { doc ->
                val collection = doc.data<CollectionFirestoreResponse>()
                val createdAtMillis = collection.createdAt?.seconds?.times(1000L)
                createdAtMillis != null && createdAtMillis in startMillis until endMillis
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
            var query = firestore.getDocumentsQuery("collections")
                .orderBy("createdAt", ExpectedFirestore.Direction.DESCENDING)
                .orderBy("__name__", ExpectedFirestore.Direction.DESCENDING) // Add tie-breaker
                .limit(limit)

            if (lastCollectionCreatedAtLong != null && lastCollectionDocumentId != null) {
                // To do startAfter with a full document, we need the actual DocumentSnapshot.
                // However, since we only have the createdAt and id, we'll try to use startAfter with these values.
                // This assumes ExpectedFirestore's startAfter can handle multiple fields.
                // If not, a workaround might be needed (e.g., fetching the last document by ID first)
                query = query.startAfter(
                    Timestamp(lastCollectionCreatedAtLong / 1000, 0)
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
