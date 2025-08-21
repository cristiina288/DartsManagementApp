package org.darts.dartsmanagement.data.locations.response

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.data.common.response.StatusResponse
import org.darts.dartsmanagement.data.locations.response.LocationResponse
import org.darts.dartsmanagement.data.machines.response.MachineResponse
import org.darts.dartsmanagement.domain.bars.models.BarModel


@Serializable
data class GetLocationsResponse (
    val locations: List<LocationResponse>
)