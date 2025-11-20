package org.darts.dartsmanagement.data.machines

import org.darts.dartsmanagement.data.common.firestore.MachineFirestoreResponse
import org.darts.dartsmanagement.data.common.firestore.UserFirestore
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore
import org.darts.dartsmanagement.data.machines.requests.SaveMachineRequest
import org.darts.dartsmanagement.data.machines.response.MachineResponse

class MachinesApiService(private val firestore: ExpectedFirestore) {

    suspend fun getMachines(): List<MachineResponse> {
        return try {
            // 1. Get the current user's UID
            val userUID = firestore.getCurrentUserUID() ?: return emptyList()

            // 2. Get the user's license_id from the 'users' collection
            val userDoc = firestore.getDocument("users", userUID)
            val user = userDoc?.data<UserFirestore>() ?: return emptyList()
            val licenseId = user.license_id

            // 3. Get all bars associated with that license_id
            val barDocs = firestore.getDocuments("bars", "license_id", licenseId)

            // 4. Get all machine_ids from the bars
            val machineIds = barDocs.flatMap {
                (it.data<Map<String, String>>()["machine_ids"])?.map { it.toString() } ?: emptyList()
            }

            // 5. Get all machines with the collected machine_ids
            if (machineIds.isEmpty()) {
                return emptyList()
            }
            val machineDocs = firestore.getDocuments("machines")
            val allMachines = machineDocs.map { doc ->
                doc.data<MachineFirestoreResponse>().copy(id = doc.id)
            }
            allMachines.filter { machine -> machineIds.contains(machine.id) }.map { it.toMachineResponse() }
        } catch (e: Exception) {
            println("/machines: $e")
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