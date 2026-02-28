package org.darts.dartsmanagement.data.common.firestore

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.data.locations.response.LocationResponse
import org.darts.dartsmanagement.domain.locations.model.LocationModel

@Serializable
data class LocationFirestoreResponse(
    val id: String = "", // Firestore document ID
    val address: String = "",
    val city: String = "",
    val name: String? = null,
    val postalCode: String? = null,
    val license_id: String = ""
) {
    fun toDomain(): LocationModel {
        return LocationModel(
            id = id,
            name = name,
            postalCode = postalCode,
            bars = emptyList()
        )
    }

    fun toLocationResponse(): LocationResponse {
        return LocationResponse(
            id = null,
            name = name,
            postalCode = postalCode
        )
    }
}
