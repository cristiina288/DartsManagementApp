package org.darts.dartsmanagement.data.collections

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.darts.dartsmanagement.data.collections.response.CollectionFirestoreResponse
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel

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
        comments: String
    ): Boolean {
        return try {
            val collectionMap = mapOf<String, Any?>(
                "machineId" to machineId,
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
            firestore.updateDocumentFields("machines", machineId.toString(), mapOf("counter" to newCounterMachine))
            true
        } catch (e: Exception) {
            println("Error saving collection: $e")
            false
        }
    }

    suspend fun getCollectionsByMachineId(
        machineId: Int
    ): List<CollectionFirestoreResponse> {
        try {
            val snapshot = firestore.getDocuments("collections", "machineId", machineId)
            return snapshot.map {
                it.data()
            }
        } catch (e: Exception) {
            println("Error getting collections by machine id: $e")
            return emptyList()
        }
    }

    suspend fun getCollectionsForMonth(year: Int, month: Int): List<CollectionFirestoreResponse> {
        return try {
            val startOfMonth = LocalDate(year, month, 1).atStartOfDayIn(TimeZone.currentSystemDefault())
            val nextMonth = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
            val startOfNextMonth = nextMonth.atStartOfDayIn(TimeZone.currentSystemDefault())

            val startMillis = startOfMonth.toEpochMilliseconds()
            val endMillis = startOfNextMonth.toEpochMilliseconds()

            val snapshot = firestore.getDocuments("collections")

            snapshot.mapNotNull { doc ->
                val collection = doc.data<CollectionFirestoreResponse>()
                
                val createdAtMillis = collection.createdAt?.seconds?.times(1000L)

                if (createdAtMillis != null && createdAtMillis in startMillis until endMillis) {
                    collection
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            println("Error getting collections for month: $e")
            emptyList()
        }
    }
}
