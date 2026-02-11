package org.darts.dartsmanagement.ui.machines.newMachine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.darts.dartsmanagement.data.bars.BarsApiService
import org.darts.dartsmanagement.data.machines.requests.SaveMachineRequest
import org.darts.dartsmanagement.domain.bars.UpdateBarWithNewMachine
import org.darts.dartsmanagement.domain.machines.SaveMachine

class NewMachineViewModel(
    private val barsApiService: BarsApiService,
    private val saveMachineUseCase: SaveMachine,
    private val updateBarWithNewMachineUseCase: UpdateBarWithNewMachine
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewMachineUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<NewMachineEvent>()
    val event = _event.asSharedFlow()

    private val _effect = MutableSharedFlow<Effect>()
    val effect = _effect.asSharedFlow()

    init {
        getAllBars()
    }

    fun onEvent(event: NewMachineEvent) {
        when (event) {
            is NewMachineEvent.OnSerialNumberChange -> _uiState.update { it.copy(serialNumber = event.serialNumber) }
            is NewMachineEvent.OnNameChange -> _uiState.update { it.copy(name = event.name) }
            is NewMachineEvent.OnCounterChange -> _uiState.update { it.copy(counter = event.counter) }
            NewMachineEvent.OnBarSelectionClick -> _uiState.update { it.copy(showBarSelectionDialog = true) }
            is NewMachineEvent.OnBarSelected -> _uiState.update {
                it.copy(
                    selectedBarId = event.barId,
                    selectedBarName = event.barName,
                    showBarSelectionDialog = false
                )
            }
            NewMachineEvent.OnDismissBarSelectionDialog -> _uiState.update { it.copy(showBarSelectionDialog = false) }
            NewMachineEvent.OnSaveMachineClick -> saveMachine()
            NewMachineEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun getAllBars() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    barsApiService.getBars()
                }
                _uiState.update { it.copy(allBars = result, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun saveMachine() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val currentUiState = _uiState.value
                if (currentUiState.serialNumber.isBlank()) {
                    _uiState.update { it.copy(error = "Serial Number cannot be empty", isLoading = false) }
                    return@launch
                }
                if (currentUiState.name.isBlank()) {
                    _uiState.update { it.copy(error = "Machine name cannot be empty", isLoading = false) }
                    return@launch
                }


                val statusId = if (currentUiState.selectedBarId != null) 1 else 2
                val statusMap = mapOf("id" to statusId)

                val request = SaveMachineRequest(
                    name = currentUiState.name,
                    counter = currentUiState.counter.toIntOrNull(),
                    barId = currentUiState.selectedBarId,
                    status = statusMap
                )

                withContext(Dispatchers.IO) {
                    saveMachineUseCase(currentUiState.serialNumber, request)
                }

                currentUiState.selectedBarId?.let { barId ->
                    withContext(Dispatchers.IO) {
                        updateBarWithNewMachineUseCase(barId, currentUiState.serialNumber)
                    }
                }

                _uiState.update { it.copy(machineSaved = true, isLoading = false) }
                _effect.emit(Effect.MachineSaved)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    sealed interface Effect {
        data object MachineSaved : Effect
    }
}
