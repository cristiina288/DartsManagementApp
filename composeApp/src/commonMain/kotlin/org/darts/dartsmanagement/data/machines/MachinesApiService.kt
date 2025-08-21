package org.darts.dartsmanagement.data.machines

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.darts.dartsmanagement.data.machines.requests.SaveMachineRequest
import org.darts.dartsmanagement.data.machines.response.GetMachinesResponse
import org.darts.dartsmanagement.data.machines.response.MachineResponse

class MachinesApiService(private val httpClient: HttpClient) {

    suspend fun getMachines(): List<MachineResponse> {

        try {
            val b: GetMachinesResponse = httpClient.get("/v1/machines").body()
            return b.machines
        } catch (e: Exception) {
            println("/machines: $e")

            try {
                val b: GetMachinesResponse = httpClient.get("/v1/machines").body()
                return b.machines
            } catch (e: Exception) {
                println("/machines: $e")
                return emptyList()
            }
        }
    }


    suspend fun saveMachine(saveMachineRequest: SaveMachineRequest) {
        try {
            httpClient.post("/v1/machines"){
                contentType(ContentType.Application.Json)
                setBody(saveMachineRequest)
            }
        } catch (e: Exception) {
            println("/machines: $e")
        }
    }
}