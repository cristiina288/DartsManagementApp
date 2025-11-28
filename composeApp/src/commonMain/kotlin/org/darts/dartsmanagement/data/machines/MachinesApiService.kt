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
        firestore.addDocument("machines", mapOf())//saveMachineRequest.toMap())
    }

    suspend fun updateMachineStatus(machineId: Int, statusId: Int) {
        val data = mapOf("status.id" to statusId)
        firestore.updateDocumentFields("machines", machineId.toString(), data)
    }
}
/*
private fun SaveMachineRequest.toMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "name" to name,
        "type" to type,
        "last_collection" to last_collection,
        "counter" to counter,
        "barId" to barId,
        "status" to mapOf("id" to statusId)
    )
}*/
