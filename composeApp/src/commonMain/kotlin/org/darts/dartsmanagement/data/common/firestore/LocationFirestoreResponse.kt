package org.darts.dartsmanagement.data.common.firestore

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.data.locations.response.LocationResponse
import org.darts.dartsmanagement.domain.locations.model.LocationModel

@Serializable
data class LocationFirestoreResponse(
    val id: String = "", // Firestore document ID
    val address: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val name: String? = null,
    val postalCode: String? = null,
    val locationBarUrl: String? = null
) {
    fun toDomain(): LocationModel {
        return LocationModel(
            id = id, // Still null as LocationModel expects Int?, but we have String
            name = name, // Directly use name from Firestore
            postalCode = postalCode,
            locationBarUrl = locationBarUrl
        )
    }

    fun toLocationResponse(): LocationResponse {
        return LocationResponse(
            id = null, // LocationResponse expects Int?, we have String id
            name = name, // Directly use name from Firestore
            postalCode = postalCode,
            locationBarUrl = locationBarUrl
        )
    }
}
