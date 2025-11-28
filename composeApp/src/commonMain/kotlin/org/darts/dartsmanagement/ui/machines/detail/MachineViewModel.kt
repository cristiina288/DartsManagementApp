package org.darts.dartsmanagement.ui.machines.detail

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
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.collections.GetCollectionsByMachineId
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.domain.common.models.Status
import org.darts.dartsmanagement.domain.machines.UpdateMachineStatusUseCase
import org.darts.dartsmanagement.domain.machines.model.MachineModel

class MachineViewModel(
    private val initialMachine: MachineModel,
    private val getCollectionsByMachineId: GetCollectionsByMachineId,
    private val updateMachineStatusUseCase: UpdateMachineStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MachineUiState(machine = initialMachine))
    val uiState: StateFlow<MachineUiState> = _uiState.asStateFlow()

    init {
        getCollectionsByMachine()
    }

    fun onEvent(event: MachineEvent) {
        when (event) {
            MachineEvent.ToggleRepairStatus -> {
                viewModelScope.launch {
                    val currentStatus = _uiState.value.machine?.status?.id ?: return@launch
                    val newStatus = if (currentStatus == Status.INACTIVE.id) Status.PENDING_REPAIR.id else Status.INACTIVE.id
                    val machineId = _uiState.value.machine?.id?.toInt() ?: return@launch

                    _uiState.update { it.copy(isLoading = true) }

                    updateMachineStatusUseCase(machineId, newStatus)
                        .onSuccess {
                            // On success, update the local state to reflect the change immediately
                            val updatedMachine = _uiState.value.machine?.copy(
                                status = _uiState.value.machine!!.status.copy(id = newStatus)
                            )
                            _uiState.update { it.copy(isLoading = false, machine = updatedMachine) }
                        }
                        .onFailure { error ->
                            _uiState.update { it.copy(isLoading = false, error = error.message) }
                        }
                }
            }
        }
    }

    private fun getCollectionsByMachine() {
        val machineId = initialMachine.id?.toInt() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = withContext(Dispatchers.IO) {
                getCollectionsByMachineId(machineId)
            }
            _uiState.update { it.copy(isLoading = false, collections = result) }
        }
    }
}

data class MachineUiState(
    val isLoading: Boolean = false,
    val machine: MachineModel? = null,
    val collections: List<CollectionModel> = emptyList(),
    val bar: BarModel? = null, // Bar info can be derived from machine if needed, or passed in
    val error: String? = null
)

sealed interface MachineEvent {
    data object ToggleRepairStatus : MachineEvent
}

