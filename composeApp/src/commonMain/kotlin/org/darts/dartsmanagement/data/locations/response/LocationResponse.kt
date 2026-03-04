package org.darts.dartsmanagement.data.locations.response

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.domain.locations.model.LocationModel


@Serializable
data class LocationResponse (
    val id: String?,
    val name: String?,
    val postalCode: String?,
    val province: String? = null
) {
    fun toDomain(): LocationModel {
        return LocationModel(
            id = id,
            name = name,
            postalCode = postalCode,
            province = province,
            bars = emptyList()
        )
    }
}