package org.darts.dartsmanagement.domain.bars.models

import org.darts.dartsmanagement.domain.common.model.StatusModel
import org.darts.dartsmanagement.domain.locations.model.LocationModel
import org.darts.dartsmanagement.domain.machines.model.MachineModel


data class BarModel (
    val id: String?,
    val name: String,
    val description: String,
    val machines: List<MachineModel>,
    val location: BarLocationDetails,
    val status: StatusModel
)

data class BarLocationDetails (
    val id: String?, // This will be the location_id linking to the locations collection
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val locationBarUrl: String?
)