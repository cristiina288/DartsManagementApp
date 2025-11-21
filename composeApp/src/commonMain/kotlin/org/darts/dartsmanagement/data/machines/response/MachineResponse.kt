package org.darts.dartsmanagement.data.machines.response

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.data.common.response.StatusResponse
import org.darts.dartsmanagement.domain.locations.model.LocationModel
import org.darts.dartsmanagement.domain.machines.model.MachineModel


@Serializable
data class MachineResponse (
    val id: Int?,
    val name: String?,
    val counter: Int?,
    val barId: String?,
    val status: StatusResponse
) {
    fun toDomain(): MachineModel {
        return MachineModel(
            id = id,
            name = name,
            counter = counter,
            barId = barId,
            status = status.toDomain()
        )
    }
}