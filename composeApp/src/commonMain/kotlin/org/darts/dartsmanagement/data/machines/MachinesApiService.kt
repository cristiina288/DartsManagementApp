package org.darts.dartsmanagement.data.machines

import org.darts.dartsmanagement.data.common.firestore.MachineFirestoreResponse
import org.darts.dartsmanagement.data.common.firestore.UserFirestore
import org.darts.dartsmanagement.data.auth.SessionManager
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore
import org.darts.dartsmanagement.data.machines.requests.SaveMachineRequest
import org.darts.dartsmanagement.data.machines.response.MachineResponse
import org.darts.dartsmanagement.domain.machines.model.MachineModel

class MachinesApiService(
    private val firestore: ExpectedFirestore,
    private val sessionManager: SessionManager
) {

    suspend fun getMachines(): List<MachineResponse> {
        return try {
            val licenseId = sessionManager.licenseId.value ?: return emptyList()
            val machineDocs = firestore.getDocuments("machines", "license_id", licenseId)
            machineDocs.map { doc ->
                doc.data<MachineFirestoreResponse>().copy(id = doc.id).toMachineResponse()
            }
        } catch (e: Exception) {
            println("Error fetching all machines: $e")
            emptyList()
        }
    }

    suspend fun getMachine(machineId: Int): MachineResponse {
        val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("License not found in session")
        val doc = firestore.getDocument("machines", machineId.toString()) ?: throw NoSuchElementException("Machine with ID $machineId not found.")
        val machineData = doc.data<MachineFirestoreResponse>().copy(id = doc.id)
        
        // Security check: Verify license_id
        if (machineData.license_id != licenseId) {
            throw IllegalStateException("You do not have permission to access this machine.")
        }
        
        return machineData.toMachineResponse()
    }


    suspend fun saveMachine(serialNumber: String, saveMachineRequest: SaveMachineRequest) {
        val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("License not found in session")
        val data = saveMachineRequest.toMap().toMutableMap()
        data["license_id"] = licenseId
        firestore.setDocument("machines", serialNumber, data)
    }

    suspend fun updateMachineStatus(machineId: Int, statusId: Int) {
        val data = mapOf("status.id" to statusId)
        firestore.updateDocumentFields("machines", machineId.toString(), data)
    }

    suspend fun updateMachine(machine: MachineModel) {
        val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("License not found in session")
        val data = mapOf(
            "name" to machine.name,
            "counter" to machine.counter,
            "barId" to machine.barId,
            "status.id" to machine.status.id,
            "license_id" to licenseId
        )
        firestore.updateDocument(
            "machines",
            machine.id.toString(),
            data
        )
    }

    suspend fun deleteMachine(machineId: Int) {
        firestore.deleteDocument("machines", machineId.toString())
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
