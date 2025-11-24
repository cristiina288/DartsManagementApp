package org.darts.dartsmanagement.data.bars.response

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.data.common.response.StatusResponse
import org.darts.dartsmanagement.data.machines.response.MachineResponse
import org.darts.dartsmanagement.domain.bars.models.BarLocationDetails
import org.darts.dartsmanagement.domain.bars.models.BarModel


@Serializable
data class BarResponse (
    val id: String?,
    val name: String,
    val description: String,
    val machines: List<MachineResponse>,
    val location: BarLocationResponse,
    val status: StatusResponse
) {
    fun toDomain(): BarModel {
        return BarModel(
            id = id,
            name = name,
            description = description,
            machines = machines.map { it.toDomain() },
            location = location.toDomain(),
            status = status.toDomain()
        )
    }
}

@Serializable
data class BarLocationResponse(
    val id: String?, // This will be the location_id linking to the locations collection
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val locationBarUrl: String?
) {
    fun toDomain(): BarLocationDetails {
        return BarLocationDetails(
            id = id,
            address = address,
            latitude = latitude,
            longitude = longitude,
            locationBarUrl = locationBarUrl
        )
    }
}