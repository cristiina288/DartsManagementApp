import dev.gitlive.firebase.firestore.Timestamp
import org.darts.dartsmanagement.data.collections.response.CollectionFirestoreResponse
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel

class CollectionsApiService(
    private val firestore: ExpectedFirestore
) {

    suspend fun saveCollection(
        collectionAmountsModel: CollectionAmountsModel,
        newCounterMachine: Int,
        machineId: Int
    ): Boolean {
        return try {
            val collection = mapOf(
                "machineId" to machineId,
                "comments" to "",
                "totalCollection" to collectionAmountsModel.totalCollection,
                "barAmount" to collectionAmountsModel.barAmount,
                "barPayment" to collectionAmountsModel.barPayment,
                "businessAmount" to collectionAmountsModel.businessAmount,
                "extraAmount" to collectionAmountsModel.extraAmount,
                "createdAt" to Timestamp.now(),
                "status" to null
            )
            firestore.addDocument("collections", collection)
            firestore.updateDocument("machines", machineId.toString(), mapOf("counter" to newCounterMachine))
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
}