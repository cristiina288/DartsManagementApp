package org.darts.dartsmanagement.data.common.firestore

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.data.locations.response.LocationResponse
import org.darts.dartsmanagement.domain.locations.model.LocationModel

@Serializable
data class LocationFirestoreResponse(
    val id: String = "", // Firestore document ID
    val name: String? = null,
    val postalCode: String? = null,
    val province: String? = null,
    val licenseId: String = ""
) {
    fun toDomain(): LocationModel {
        return LocationModel(
            id = id,
            name = name,
            postalCode = postalCode,
            province = province,
            licenseId = licenseId,
            bars = emptyList()
        )
    }

    fun toLocationResponse(): LocationResponse {
        return LocationResponse(
            id = id,
            name = name,
            postalCode = postalCode,
            province = province
        )
    }
}
