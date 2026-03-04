
package org.darts.dartsmanagement.data.bars

import org.darts.dartsmanagement.data.bars.requests.SaveBarRequest
import org.darts.dartsmanagement.data.bars.response.BarLocationResponse
import org.darts.dartsmanagement.data.bars.response.BarResponse
import org.darts.dartsmanagement.data.common.firestore.BarFirestoreResponse
import org.darts.dartsmanagement.data.common.firestore.BarLocationFirestoreDetails
import org.darts.dartsmanagement.data.common.firestore.MachineFirestoreResponse
import org.darts.dartsmanagement.data.common.firestore.UserFirestore
import org.darts.dartsmanagement.data.auth.SessionManager
import org.darts.dartsmanagement.data.common.response.StatusResponse
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore

class BarsApiService(
    private val firestore: ExpectedFirestore,
    private val sessionManager: SessionManager
) {

        suspend fun getBars(): List<BarResponse> {
            // 1. Get the license_id from SessionManager
            val licenseId = sessionManager.licenseId.value ?: return emptyList()

            // 2. Get all bars associated with that license_id
            val barDocs = firestore.getDocuments("bars", "license_id", licenseId)
            val barsFirestore = barDocs.map { it.data<BarFirestoreResponse>().copy(id = it.id) }

            val allMachinesDocs = firestore.getDocuments("machines") // Assuming a method to get all documents in a collection
            val allMachines = allMachinesDocs.mapNotNull { doc ->
                doc.data<MachineFirestoreResponse>().copy(id = doc.id)
            }

            return barsFirestore.mapNotNull { barFirestore ->
                // Now, location details are embedded within barFirestore.location
                val machinesFirestoreResponses = allMachines.filter { machine -> barFirestore.machine_ids.any { it.toString() == machine.id } }

                BarResponse(
                    id = barFirestore.id,
                    name = barFirestore.name,
                    description = barFirestore.description,
                    location = BarLocationResponse(
                        id = barFirestore.location.id,
                        address = barFirestore.location.address,
                        latitude = barFirestore.location.latitude,
                        longitude = barFirestore.location.longitude,
                        locationBarUrl = barFirestore.location.locationBarUrl
                    ),
                    machines = machinesFirestoreResponses.map { it.toMachineResponse() },
                    status = StatusResponse(id = barFirestore.status_id.toInt())
                )
            }
        }

    suspend fun saveBar(saveBarRequest: SaveBarRequest) {
        try {
            // 1. Get the license_id from SessionManager
            val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("License not found in session")

            // 2. Create BarFirestoreResponse from SaveBarRequest
            val barData = mapOf(
                //"id" to saveBarRequest.id,
                "name" to saveBarRequest.name,
                "description" to saveBarRequest.description,
                "license_id" to licenseId,
                "location" to mapOf(
                    "id" to saveBarRequest.locationId, // Assuming saveBarRequest will have a locationId
                    "address" to saveBarRequest.address,
                    "latitude" to saveBarRequest.latitude,
                    "longitude" to saveBarRequest.longitude,
                    "locationBarUrl" to saveBarRequest.locationBarUrl
                ),
                "status_id" to saveBarRequest.statusId,
                "machine_ids" to saveBarRequest.machineIds
            )
            firestore.addDocument("bars", barData)
        } catch (e: Exception) {
            println("Error saving bar: $e")
            throw e
        }
    }

    suspend fun updateBar(barId: String, saveBarRequest: SaveBarRequest) {
        try {
            // 1. Get the license_id from SessionManager
            val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("License not found in session")

            // 2. Create Update map from SaveBarRequest
            val barData = mapOf(
                "name" to saveBarRequest.name,
                "description" to saveBarRequest.description,
                "license_id" to licenseId,
                "location" to mapOf(
                    "id" to saveBarRequest.locationId,
                    "address" to saveBarRequest.address,
                    "latitude" to saveBarRequest.latitude,
                    "longitude" to saveBarRequest.longitude,
                    "locationBarUrl" to saveBarRequest.locationBarUrl
                ),
                "status_id" to saveBarRequest.statusId,
                "machine_ids" to saveBarRequest.machineIds
            )
            firestore.updateDocument("bars", barId, barData)
        } catch (e: Exception) {
            println("Error updating bar: $e")
            throw e
        }
    }

    suspend fun getBar(barId: String): BarResponse {
        val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("License not found in session")
        
        val barDoc = firestore.getDocument("bars", barId) ?: throw NoSuchElementException("Bar with ID $barId not found.")
        val barFirestore = barDoc.data<BarFirestoreResponse>().copy(id = barDoc.id)
        
        // Security check: Verify license_id
        if (barFirestore.license_id != licenseId) {
            throw IllegalStateException("You do not have permission to access this bar.")
        }

        val allMachinesDocs = firestore.getDocuments("machines")
        val allMachines = allMachinesDocs.mapNotNull { doc ->
            doc.data<MachineFirestoreResponse>().copy(id = doc.id)
        }

        val machinesFirestoreResponses = allMachines.filter { machine -> barFirestore.machine_ids.any { it.toString() == machine.id } }

        return BarResponse(
            id = barFirestore.id,
            /*name = barFirestore.name,
            description = barFirestore.description,
            location = BarLocationResponse(
                id = barFirestore.location.id,
                address = barFirestore.location.address,
                latitude = barFirestore.location.latitude,
                longitude = barFirestore.location.longitude,
                locationBarUrl = barFirestore.location.locationBarUrl
            ),
            machines = machinesFirestoreResponses.map { it.toMachineResponse() },
            status = StatusResponse(id = barFirestore.status_id.toInt())
        )
    }*/
            name = barFirestore.name,
            description = barFirestore.description,
            location = BarLocationResponse(
                id = barFirestore.location.id,
                address = barFirestore.location.address,
                latitude = barFirestore.location.latitude,
                longitude = barFirestore.location.longitude,
                locationBarUrl = barFirestore.location.locationBarUrl
            ),
            machines = machinesFirestoreResponses.map { it.toMachineResponse() },
            status = StatusResponse(id = barFirestore.status_id.toInt())
        )
    }

    suspend fun updateBarMachines(barId: String, machineIds: List<Int>) {
        try {
            val dataToUpdate = mapOf("machine_ids" to machineIds)
            firestore.updateDocumentFields("bars", barId, dataToUpdate)
        } catch (e: Exception) {
            println("Error updating bar machines: $e")
            throw e
        }
    }

    suspend fun deleteBar(barId: String) {
        firestore.deleteDocument("bars", barId)
    }
}