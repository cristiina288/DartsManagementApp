package org.darts.dartsmanagement.data.bars.response

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.data.common.response.StatusResponse
import org.darts.dartsmanagement.data.locations.response.LocationResponse
import org.darts.dartsmanagement.data.machines.response.MachineResponse
import org.darts.dartsmanagement.domain.bars.models.BarModel


@Serializable
data class BarResponse (
    val id: String?,
    val name: String,
    val description: String,
    val machines: List<MachineResponse>,
    val location: LocationResponse,
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