package org.darts.dartsmanagement.ui.machines.listing

import org.darts.dartsmanagement.domain.machines.model.MachineModel

data class MachinesUiModel(
    val machine: MachineModel,
    val barName: String?
)
