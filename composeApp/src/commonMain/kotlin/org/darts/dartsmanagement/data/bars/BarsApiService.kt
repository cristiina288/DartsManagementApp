
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
            // 1. Get the licenseId from SessionManager
            val licenseId = sessionManager.licenseId.value ?: return emptyList()

            // 2. Get all bars associated with that licenseId
            val barDocs = firestore.getDocuments("bars", "licenseId", licenseId)
            val barsFirestore = barDocs.map { it.data<BarFirestoreResponse>().copy(id = it.id) }

            val allMachinesDocs = firestore.getDocuments("machines") 
            val allMachines = allMachinesDocs.mapNotNull { doc ->
                doc.data<MachineFirestoreResponse>().copy(id = doc.id)
            }

            return barsFirestore.mapNotNull { barFirestore ->
                val machinesFirestoreResponses = allMachines.filter { machine -> barFirestore.machineIds.any { it.toString() == machine.id } }

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
                    status = barFirestore.status,
                    licenseId = barFirestore.licenseId
                )
            }
        }

    suspend fun saveBar(saveBarRequest: SaveBarRequest): String {
        return try {
            val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("License not found in session")

            val barData = mapOf(
                "name" to saveBarRequest.name,
                "description" to saveBarRequest.description,
                "licenseId" to licenseId,
                "location" to mapOf(
                    "id" to saveBarRequest.locationId,
                    "address" to saveBarRequest.address,
                    "latitude" to saveBarRequest.latitude,
                    "longitude" to saveBarRequest.longitude,
                    "locationBarUrl" to saveBarRequest.locationBarUrl
                ),
                "status" to saveBarRequest.status,
                "machineIds" to saveBarRequest.machineIds
            )
            firestore.addDocument("bars", barData)
        } catch (e: Exception) {
            println("Error saving bar: $e")
            throw e
        }
    }

    suspend fun updateBar(barId: String, saveBarRequest: SaveBarRequest) {
        try {
            val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("License not found in session")

            val barData = mapOf(
                "name" to saveBarRequest.name,
                "description" to saveBarRequest.description,
                "licenseId" to licenseId,
                "location" to mapOf(
                    "id" to saveBarRequest.locationId,
                    "address" to saveBarRequest.address,
                    "latitude" to saveBarRequest.latitude,
                    "longitude" to saveBarRequest.longitude,
                    "locationBarUrl" to saveBarRequest.locationBarUrl
                ),
                "status" to saveBarRequest.status,
                "machineIds" to saveBarRequest.machineIds
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
        
        // Security check: Verify licenseId
        if (barFirestore.licenseId != licenseId) {
            throw IllegalStateException("You do not have permission to access this bar.")
        }

        val allMachinesDocs = firestore.getDocuments("machines")
        val allMachines = allMachinesDocs.mapNotNull { doc ->
            doc.data<MachineFirestoreResponse>().copy(id = doc.id)
        }

        val machinesFirestoreResponses = allMachines.filter { machine -> barFirestore.machineIds.any { it.toString() == machine.id } }

        return BarResponse(
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
            status = barFirestore.status,
            licenseId = barFirestore.licenseId
        )
    }

    suspend fun updateBarMachines(barId: String, machineIds: List<Int>) {
        try {
            val dataToUpdate = mapOf("machineIds" to machineIds)
            firestore.updateDocumentFields("bars", barId, dataToUpdate)
        } catch (e: Exception) {
            println("Error updating bar machines: $e")
            throw e
        }
    }

    private fun mapStatusStringToId(status: String): Int {
        return when (status.lowercase()) {
            "active" -> 1
            "inactive" -> 2
            "pending repair" -> 3
            else -> 0
        }
    }

    private fun mapStatusIdToString(statusId: Int): String {
        return when (statusId) {
            1 -> "active"
            2 -> "inactive"
            3 -> "pending repair"
            else -> "active"
        }
    }

    suspend fun deleteBar(barId: String) {
        firestore.deleteDocument("bars", barId)
    }
}