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
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.collections.GetCollectionsByMachineId
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.domain.common.models.Status
import org.darts.dartsmanagement.domain.machines.MachinesRepository
import org.darts.dartsmanagement.domain.machines.UpdateMachineStatusUseCase
import org.darts.dartsmanagement.domain.machines.DeleteMachineUseCase
import org.darts.dartsmanagement.domain.machines.model.MachineModel

class MachineViewModel(
    private val initialMachine: MachineModel,
    private val getCollectionsByMachineId: GetCollectionsByMachineId,
    private val updateMachineStatusUseCase: UpdateMachineStatusUseCase,
    private val machinesRepository: MachinesRepository,
    private val deleteMachineUseCase: DeleteMachineUseCase,
    private val getBars: GetBars
) : ViewModel() {

    private val _uiState = MutableStateFlow(MachineUiState(machine = initialMachine))
    val uiState: StateFlow<MachineUiState> = _uiState.asStateFlow()

    init {
        refresh()
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
            MachineEvent.OnRefresh -> refresh()
            MachineEvent.DeleteMachine -> {
                viewModelScope.launch {
                    val machineId = _uiState.value.machine?.id?.toInt() ?: return@launch
                    _uiState.update { it.copy(isLoading = true) }
                    deleteMachineUseCase(machineId)
                        .onSuccess {
                            _uiState.update { it.copy(isLoading = false, isDeleted = true) }
                        }
                        .onFailure { error ->
                            _uiState.update { it.copy(isLoading = false, error = error.message) }
                        }
                }
            }
        }
    }

    private fun refresh() {
        val machineId = initialMachine.id?.toInt() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // 1. Refresh Machine Details
            machinesRepository.getMachine(machineId).onSuccess { updatedMachine ->
                _uiState.update { it.copy(machine = updatedMachine) }
                
                // 2. Refresh Bar Details if assigned
                if (!updatedMachine.barId.isNullOrEmpty()) {
                    try {
                        val bars = getBars()
                        val assignedBar = bars.find { it.id == updatedMachine.barId }
                        _uiState.update { it.copy(bar = assignedBar) }
                    } catch (e: Exception) {
                        println("Error fetching bar for machine: $e")
                    }
                } else {
                    _uiState.update { it.copy(bar = null) }
                }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }

            // 3. Refresh Collections
            val collections = withContext(Dispatchers.IO) {
                getCollectionsByMachineId(machineId)
            }
            _uiState.update { it.copy(isLoading = false, collections = collections) }
        }
    }

    private fun getCollectionsByMachine() {
        // Now handled by refresh()
    }
}

data class MachineUiState(
    val isLoading: Boolean = false,
    val machine: MachineModel? = null,
    val collections: List<CollectionModel> = emptyList(),
    val bar: BarModel? = null,
    val error: String? = null,
    val isDeleted: Boolean = false
)

sealed interface MachineEvent {
    data object ToggleRepairStatus : MachineEvent
    data object OnRefresh : MachineEvent
    data object DeleteMachine : MachineEvent
}

