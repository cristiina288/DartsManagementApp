package org.darts.dartsmanagement.data.collections

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.darts.dartsmanagement.data.collections.requests.CollectionAmountsRequest
import org.darts.dartsmanagement.data.collections.requests.CollectionRequest
import org.darts.dartsmanagement.data.collections.requests.SaveCollectionRequest
import org.darts.dartsmanagement.data.collections.response.CollectionResponse
import org.darts.dartsmanagement.data.collections.response.GetCollectionsByMachineIdResponse
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel

class CollectionsApiService(private val httpClient: HttpClient) {

    suspend fun saveCollection(
        collectionAmountsModel: CollectionAmountsModel,
        newCounterMachine: Int
    ) {

        try {
            val request = SaveCollectionRequest(
                collection = CollectionRequest(
                    id = 0,
                    machineId = 2,
                    comments = "",
                    collectionAmounts = CollectionAmountsRequest(
                        totalCollection = collectionAmountsModel.totalCollection,
                        barAmount = collectionAmountsModel.barAmount,
                        barPayment = collectionAmountsModel.barPayment,
                        businessAmount = collectionAmountsModel.businessAmount,
                        extraAmount = collectionAmountsModel.extraAmount
                    ),
                    status = null
                ),
                counterMachine = newCounterMachine
            )

            httpClient.post("/v1/collections") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        } catch (e: Exception) {
            println("/collections: $e")
        }
    }


    suspend fun getCollectionsById(
        machineId: Int
    ) : List<CollectionResponse> {
        try {
            val b: GetCollectionsByMachineIdResponse = httpClient.get("/v1/collections/by-machine/$machineId").body()
            return b.collections
        } catch (e: Exception) {
            println("/collections/by-machine: $e")
            return emptyList()
        }
    }
}