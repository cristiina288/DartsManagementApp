package org.darts.dartsmanagement.data.locations

import org.darts.dartsmanagement.data.common.firestore.LocationFirestoreResponse
import org.darts.dartsmanagement.data.common.firestore.UserFirestore
import org.darts.dartsmanagement.data.common.firestore.BarFirestoreResponse
import org.darts.dartsmanagement.data.locations.requests.SaveLocationRequest
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore

class LocationsApiService(private val firestore: ExpectedFirestore) {

    suspend fun getLocations(): List<LocationFirestoreResponse> {
        return try {
            // Fetch all documents from the 'locations' collection
            val locationDocs = firestore.getDocuments("locations")
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
            val locationData = mapOf(
                "name" to saveLocationRequest.name,
                "postalCode" to saveLocationRequest.postalCode
            )
            firestore.addDocument("locations", locationData)
        } catch (e: Exception) {
            println("/saveLocation: $e")
            ""
        }
    }
}