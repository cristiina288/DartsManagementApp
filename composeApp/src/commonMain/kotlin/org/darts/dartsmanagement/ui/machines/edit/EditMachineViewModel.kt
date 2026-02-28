package org.darts.dartsmanagement.ui.machines.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.data.bars.response.BarResponse
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.machines.model.MachineModel
import org.darts.dartsmanagement.domain.machines.usecases.UpdateMachineUseCase


class EditMachineViewModel(
    private val machine: MachineModel,
    private val getBars: GetBars,
    private val updateMachineUseCase: UpdateMachineUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditMachineUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        _uiState.update {
            it.copy(
                serialNumber = machine.id.toString(),
                name = machine.name.orEmpty(),
                counter = machine.counter.toString(),
                selectedBarId = machine.barId,
            )
        }
        loadBars()
    }

    private fun loadBars() {
        viewModelScope.launch {
            try {
                val bars = getBars()
                _uiState.update {
                    it.copy(
                        allBars = bars,
                        selectedBarName = bars.find { it.id.toString() == machine.barId }?.name
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error loading bars: ${e.message}") }
            }
        }
    }


    fun onEvent(event: EditMachineEvent) {
        when (event) {
            is EditMachineEvent.OnNameChange -> _uiState.update { it.copy(name = event.name) }
            is EditMachineEvent.OnCounterChange -> _uiState.update { it.copy(counter = event.counter) }
            is EditMachineEvent.OnBarSelectionClick -> _uiState.update { it.copy(showBarSelectionDialog = true) }
            is EditMachineEvent.OnDismissBarSelectionDialog -> _uiState.update {
                it.copy(
                    showBarSelectionDialog = false
                )
            }

            is EditMachineEvent.OnBarSelected -> _uiState.update {
                it.copy(
                    selectedBarId = event.barId,
                    selectedBarName = event.barName,
                    showBarSelectionDialog = false
                )
            }

            is EditMachineEvent.OnClearBarSelection -> _uiState.update {
                it.copy(
                    selectedBarId = "",
                    selectedBarName = null
                )
            }

            is EditMachineEvent.OnSaveMachineClick -> saveMachine()
            is EditMachineEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun saveMachine() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentState = _uiState.value
                val updatedMachine = machine.copy(
                    name = currentState.name,
                    counter = currentState.counter.toIntOrNull() ?: 0,
                    barId = currentState.selectedBarId ?: ""
                )
                updateMachineUseCase(updatedMachine, machine.barId)
                _effect.send(Effect.MachineSaved)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error saving machine: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }


    sealed class Effect {
        data object MachineSaved : Effect()
    }
}

data class EditMachineUiState(
    val serialNumber: String = "",
    val name: String = "",
    val counter: String = "",
    val selectedBarId: String? = null,
    val selectedBarName: String? = null,
    val allBars: List<BarModel> = emptyList(),
    val showBarSelectionDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface EditMachineEvent {
    data class OnNameChange(val name: String) : EditMachineEvent
    data class OnCounterChange(val counter: String) : EditMachineEvent
    data object OnBarSelectionClick : EditMachineEvent
    data object OnDismissBarSelectionDialog : EditMachineEvent
    data class OnBarSelected(val barId: String, val barName: String) : EditMachineEvent
    data object OnClearBarSelection : EditMachineEvent
    data object OnSaveMachineClick : EditMachineEvent
    data object ClearError : EditMachineEvent
}
