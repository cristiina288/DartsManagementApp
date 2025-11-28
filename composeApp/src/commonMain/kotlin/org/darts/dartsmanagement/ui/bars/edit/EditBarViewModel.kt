package org.darts.dartsmanagement.ui.bars.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.darts.dartsmanagement.domain.bars.UpdateBarMachinesUseCase
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.machines.GetMachines
import org.darts.dartsmanagement.domain.machines.model.MachineModel


class EditBarViewModel(
    private val initialBarModel: BarModel,
    private val getMachines: GetMachines,
    private val updateBarMachinesUseCase: UpdateBarMachinesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditBarUiState(bar = initialBarModel, selectedMachineIds = initialBarModel.machines.mapNotNull { it.id }.toSet()))
    val uiState: StateFlow<EditBarUiState> = _uiState.asStateFlow()

    init {
        getAllMachines()
    }

    private fun getAllMachines() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val machines = withContext(Dispatchers.IO) {
                getMachines()
            }
            _uiState.update { it.copy(isLoading = false, allMachines = machines) }
        }
    }

    fun onEvent(event: EditBarEvent) {
        when (event) {
            is EditBarEvent.ToggleMachineSelection -> {
                _uiState.update { currentUiState ->
                    val newSelectedMachineIds = currentUiState.selectedMachineIds.toMutableSet()
                    if (newSelectedMachineIds.contains(event.machineId)) {
                        newSelectedMachineIds.remove(event.machineId)
                    } else {
                        newSelectedMachineIds.add(event.machineId)
                    }
                    currentUiState.copy(selectedMachineIds = newSelectedMachineIds)
                }
                updateBarMachinesInState()
            }
            is EditBarEvent.RemoveMachineFromBar -> {
                _uiState.update { currentUiState ->
                    val newSelectedMachineIds = currentUiState.selectedMachineIds.toMutableSet()
                    newSelectedMachineIds.remove(event.machineId)
                    currentUiState.copy(selectedMachineIds = newSelectedMachineIds)
                }
                updateBarMachinesInState()
            }
            EditBarEvent.OnSaveTapped -> {
                _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
                viewModelScope.launch {
                    val currentBarId = uiState.value.bar?.id
                    val machineIdsToSave = uiState.value.selectedMachineIds.toList()

                    if (currentBarId != null) {
                        updateBarMachinesUseCase(currentBarId, machineIdsToSave)
                            .onSuccess {
                                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                            }
                            .onFailure { error ->
                                _uiState.update { it.copy(isSaving = false, error = error.message) }
                            }
                    } else {
                        _uiState.update { it.copy(isSaving = false, error = "Bar ID not found") }
                    }
                }
            }
        }
    }

    private fun updateBarMachinesInState() {
        _uiState.update { currentUiState ->
            val newMachines = currentUiState.allMachines.filter { currentUiState.selectedMachineIds.contains(it.id) }
            currentUiState.bar?.copy(machines = newMachines)
                ?.let { updatedBar -> currentUiState.copy(bar = updatedBar) }
                ?: currentUiState // If bar is null, return current state
        }
    }
}

data class EditBarUiState(
    val isLoading: Boolean = false,
    val bar: BarModel? = null,
    val allMachines: List<MachineModel> = emptyList(),
    val selectedMachineIds: Set<Int> = emptySet(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

sealed interface EditBarEvent {
    data class ToggleMachineSelection(val machineId: Int) : EditBarEvent
    data class RemoveMachineFromBar(val machineId: Int) : EditBarEvent
    data object OnSaveTapped : EditBarEvent
}
