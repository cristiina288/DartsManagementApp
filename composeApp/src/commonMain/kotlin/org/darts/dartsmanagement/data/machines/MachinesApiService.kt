package org.darts.dartsmanagement.data.machines

import org.darts.dartsmanagement.data.common.firestore.MachineFirestoreResponse
import org.darts.dartsmanagement.data.common.firestore.UserFirestore
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore
import org.darts.dartsmanagement.data.machines.requests.SaveMachineRequest
import org.darts.dartsmanagement.data.machines.response.MachineResponse
import org.darts.dartsmanagement.domain.machines.model.MachineModel

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

    suspend fun getMachine(machineId: Int): MachineResponse {
        val doc = firestore.getDocument("machines", machineId.toString()) ?: throw NoSuchElementException("Machine with ID $machineId not found.")
        return doc.data<MachineFirestoreResponse>().copy(id = doc.id).toMachineResponse()
    }


    suspend fun saveMachine(serialNumber: String, saveMachineRequest: SaveMachineRequest) {
        firestore.setDocument("machines", serialNumber, saveMachineRequest.toMap())
    }

    suspend fun updateMachineStatus(machineId: Int, statusId: Int) {
        val data = mapOf("status.id" to statusId)
        firestore.updateDocumentFields("machines", machineId.toString(), data)
    }

    suspend fun updateMachine(machine: MachineModel) {
        val data = mapOf(
            "name" to machine.name,
            //"type" to machine.type, // Assuming type is part of MachineModel and needs to be updated
            //"last_collection" to machine.lastCollection, // Assuming lastCollection is part of MachineModel
            "counter" to machine.counter,
            "barId" to machine.barId,
            "status.id" to machine.status.id
        )
        firestore.updateDocument(
            "machines",
            machine.id.toString(),
            data
        )
    }
}

private fun SaveMachineRequest.toMap(): Map<String, Any?> {
    return mapOf(
        "name" to name,
        "counter" to counter,
        "barId" to barId,
        "status" to status
    )
}
