package org.darts.dartsmanagement.data.bars

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.darts.dartsmanagement.data.bars.requests.SaveBarRequest
import org.darts.dartsmanagement.data.bars.response.BarResponse
import org.darts.dartsmanagement.data.bars.response.GetBarResponse
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore

class BarsApiService(private val httpClient: HttpClient, private val firestore: ExpectedFirestore) {

    suspend fun getBars(): List<BarResponse> {

        try {
            val b: GetBarResponse = httpClient.get("/v1/bars").body()
            return b.bars
        } catch (e: Exception) {
            println("/bar: $e")

            try {
                val b: GetBarResponse = httpClient.get("/v1/bars").body()
                return b.bars
            } catch (e: Exception) {
                println("/bar: $e")
                return emptyList()
            }
        }
    }


    suspend fun saveBar(saveBarRequest: SaveBarRequest) {
        try {
            val barMap = mapOf(
                "name" to saveBarRequest.name,
                "address" to saveBarRequest.address,
                "latitude" to saveBarRequest.latitude,
                "longitude" to saveBarRequest.longitude
            )
            firestore.addDocument("bars", barMap)
        } catch (e: Exception) {
            println("/bar: $e")
        }
    }
}