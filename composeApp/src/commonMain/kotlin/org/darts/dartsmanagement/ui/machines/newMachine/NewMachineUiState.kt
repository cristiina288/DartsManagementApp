package org.darts.dartsmanagement.ui.machines.newMachine

import org.darts.dartsmanagement.data.bars.response.BarResponse

data class NewMachineUiState(
    val serialNumber: String = "",
    val name: String = "",
    val counter: String = "",
    val selectedBarId: String? = null,
    val selectedBarName: String? = null,
    val allBars: List<BarResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showBarSelectionDialog: Boolean = false,
    val machineSaved: Boolean = false
)

sealed interface NewMachineEvent {
    data class OnSerialNumberChange(val serialNumber: String) : NewMachineEvent
    data class OnNameChange(val name: String) : NewMachineEvent
    data class OnCounterChange(val counter: String) : NewMachineEvent
    data object OnBarSelectionClick : NewMachineEvent
    data class OnBarSelected(val barId: String, val barName: String) : NewMachineEvent
    data object OnDismissBarSelectionDialog : NewMachineEvent
    data object OnSaveMachineClick : NewMachineEvent
    data object ClearError : NewMachineEvent
}
