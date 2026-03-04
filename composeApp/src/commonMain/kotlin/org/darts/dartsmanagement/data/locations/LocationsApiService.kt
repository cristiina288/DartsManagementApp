package org.darts.dartsmanagement.data.locations

import org.darts.dartsmanagement.data.common.firestore.LocationFirestoreResponse
import org.darts.dartsmanagement.data.common.firestore.UserFirestore
import org.darts.dartsmanagement.data.common.firestore.BarFirestoreResponse
import org.darts.dartsmanagement.data.auth.SessionManager
import org.darts.dartsmanagement.data.locations.requests.SaveLocationRequest
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore

class LocationsApiService(
    private val firestore: ExpectedFirestore,
    private val sessionManager: SessionManager
) {

    suspend fun getLocations(): List<LocationFirestoreResponse> {
        return try {
            val licenseId = sessionManager.licenseId.value ?: return emptyList()
            
            // Fetch documents from the 'locations' collection filtered by licenseId
            val locationDocs = firestore.getDocuments("locations", "licenseId", licenseId)
            val allLocations = locationDocs.map { doc ->
                doc.data<LocationFirestoreResponse>().copy(id = doc.id)
            }
            allLocations
        } catch (e: Exception) {
            println("/locations: $e")
            emptyList()
        }
    }

    suspend fun saveLocation(saveLocationRequest: SaveLocationRequest): String {
        return try {
            val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("License not found in session")
            
            val locationData = mapOf(
                "name" to saveLocationRequest.name,
                "postalCode" to saveLocationRequest.postalCode,
                "province" to saveLocationRequest.province,
                "licenseId" to licenseId
            )
            firestore.addDocument("locations", locationData)
        } catch (e: Exception) {
            println("/saveLocation: $e")
            ""
        }
    }

    suspend fun updateLocation(locationId: String, saveLocationRequest: SaveLocationRequest) {
        try {
            val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("License not found in session")
            
            val locationData = mapOf(
                "name" to saveLocationRequest.name,
                "postalCode" to saveLocationRequest.postalCode,
                "province" to saveLocationRequest.province,
                "licenseId" to licenseId
            )
            firestore.updateDocument("locations", locationId, locationData)
        } catch (e: Exception) {
            println("/updateLocation: $e")
            throw e
        }
    }
}