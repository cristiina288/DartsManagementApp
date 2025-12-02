package org.darts.dartsmanagement.ui.bars.newBar

import org.darts.dartsmanagement.domain.locations.model.LocationModel
import org.darts.dartsmanagement.domain.machines.model.MachineModel

data class NewBarUiState(
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val locationBarUrl: String = "",
    val selectedLocation: LocationModel? = null,
    val locations: List<LocationModel> = emptyList(),
    val assignedMachines: List<MachineModel> = emptyList(),
    val allMachines: List<MachineModel> = emptyList(),
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val selectedMachineIds: MutableSet<Int> = mutableSetOf()
)