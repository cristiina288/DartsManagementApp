package org.darts.dartsmanagement.data.bars

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.darts.dartsmanagement.data.bars.requests.SaveBarRequest
import org.darts.dartsmanagement.data.bars.response.BarResponse
import org.darts.dartsmanagement.data.bars.response.GetBarResponse

class BarsApiService(private val httpClient: HttpClient) {

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
            httpClient.post("/v1/bars"){
                contentType(ContentType.Application.Json)
                setBody(saveBarRequest)
            }
        } catch (e: Exception) {
            println("/bar: $e")
        }
    }
}