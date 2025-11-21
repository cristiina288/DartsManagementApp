package org.darts.dartsmanagement.data.machines

import org.darts.dartsmanagement.data.common.firestore.MachineFirestoreResponse
import org.darts.dartsmanagement.data.common.firestore.UserFirestore
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore
import org.darts.dartsmanagement.data.machines.requests.SaveMachineRequest
import org.darts.dartsmanagement.data.machines.response.MachineResponse

class MachinesApiService(private val firestore: ExpectedFirestore) {

    suspend fun getMachines(): List<MachineResponse> {
        return try {
            val machineDocs = firestore.getDocuments("machines")
            machineDocs.map { doc ->
                doc.data<MachineFirestoreResponse>().copy(id = doc.id).toMachineResponse()
            }
        } catch (e: Exception) {
            println("Error fetching all machines: $e")
            emptyList()
        }
    }


    suspend fun saveMachine(saveMachineRequest: SaveMachineRequest) {
        // TODO: Implement saveMachine with Firestore
        /*
        try {
            httpClient.post("/v1/machines"){
                contentType(ContentType.Application.Json)
                setBody(saveMachineRequest)
            }
        } catch (e: Exception) {
            println("/machines: $e")
        }
        */
    }
}