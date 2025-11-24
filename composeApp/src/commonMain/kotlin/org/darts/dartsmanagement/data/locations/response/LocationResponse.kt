package org.darts.dartsmanagement.data.locations.response

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.domain.locations.model.LocationModel


@Serializable
data class LocationResponse (
    val id: Int?,
    val name: String?,
    val postalCode: String?
) {
    fun toDomain(): LocationModel {
        return LocationModel(
            id = id.toString(), //todo pending delete this
            name = name,
            postalCode = postalCode,
            bars = emptyList()
        )
    }
}