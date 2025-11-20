package org.darts.dartsmanagement.data.locations

import org.darts.dartsmanagement.data.common.firestore.LocationFirestoreResponse
import org.darts.dartsmanagement.data.common.firestore.UserFirestore
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore

class LocationsApiService(private val firestore: ExpectedFirestore) {

    suspend fun getLocations(): List<LocationFirestoreResponse> {
        return try {
            // 1. Get the current user's UID
            val userUID = firestore.getCurrentUserUID() ?: return emptyList()

            // 2. Get the user's license_id from the 'users' collection
            val userDoc = firestore.getDocument("users", userUID)
            val user = userDoc?.data<UserFirestore>() ?: return emptyList()
            val licenseId = user.license_id

            // 3. Get all bars associated with that license_id
            val barDocs = firestore.getDocuments("bars", "license_id", licenseId)

            // 4. Get all location_ids from the bars
            val locationIds = barDocs.mapNotNull {
                it.data<Map<String, String>>()["location_id"]
            }

            // 5. Get all locations with the collected location_ids
            if (locationIds.isEmpty()) {
                return emptyList()
            }
            val locationDocs = firestore.getDocuments("locations")
            val allLocations = locationDocs.map { doc ->
                doc.data<LocationFirestoreResponse>().copy(id = doc.id)
            }
            allLocations.filter { location -> locationIds.contains(location.id) }//.map { it.toLocationResponse() }
        } catch (e: Exception) {
            println("/locations: $e")
            emptyList()
        }
    }
}