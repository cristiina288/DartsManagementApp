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
import org.darts.dartsmanagement.data.bars.requests.SaveBarRequest
import org.darts.dartsmanagement.domain.bars.DeleteBarUseCase
import org.darts.dartsmanagement.domain.bars.UpdateBarMachinesUseCase
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.locations.GetLocations
import org.darts.dartsmanagement.domain.locations.model.LocationModel
import org.darts.dartsmanagement.domain.machines.GetMachines
import org.darts.dartsmanagement.domain.machines.model.MachineModel


class EditBarViewModel(
    private val initialBarModel: BarModel,
    private val getMachines: GetMachines,
    private val getLocations: GetLocations,
    private val updateBarMachinesUseCase: UpdateBarMachinesUseCase,
    private val deleteBarUseCase: DeleteBarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditBarUiState(
            bar = initialBarModel,
            selectedMachineIds = initialBarModel.machines.mapNotNull { it.id }.toSet(),
            name = initialBarModel.name,
            description = initialBarModel.description ?: "",
            address = initialBarModel.location.address ?: "",
            latitude = initialBarModel.location.latitude?.toString() ?: "",
            longitude = initialBarModel.location.longitude?.toString() ?: "",
            locationBarUrl = initialBarModel.location.locationBarUrl ?: ""
        )
    )
    val uiState: StateFlow<EditBarUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val machines = withContext(Dispatchers.IO) { getMachines() }
                val locations = withContext(Dispatchers.IO) { getLocations() }
                
                val selectedLoc = locations.find { it.id == initialBarModel.location.id }

                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        allMachines = machines, 
                        locations = locations,
                        selectedLocation = selectedLoc
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onEvent(event: EditBarEvent) {
        when (event) {
            is EditBarEvent.OnNameChanged -> _uiState.update { it.copy(name = event.name) }
            is EditBarEvent.OnDescriptionChanged -> _uiState.update { it.copy(description = event.description) }
            is EditBarEvent.OnAddressChanged -> _uiState.update { it.copy(address = event.address) }
            is EditBarEvent.OnLatitudeChanged -> _uiState.update { it.copy(latitude = event.latitude) }
            is EditBarEvent.OnLongitudeChanged -> _uiState.update { it.copy(longitude = event.longitude) }
            is EditBarEvent.OnLocationBarUrlChanged -> _uiState.update { it.copy(locationBarUrl = event.url) }
            is EditBarEvent.OnLocationSelected -> {
                val location = _uiState.value.locations.find { it.id == event.locationId }
                _uiState.update { it.copy(selectedLocation = location) }
            }
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
                saveBar()
            }
            EditBarEvent.DeleteBar -> {
                deleteBar()
            }
        }
    }

    private fun deleteBar() {
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val currentBarId = _uiState.value.bar?.id
            if (currentBarId == null) {
                _uiState.update { it.copy(isSaving = false, error = "Bar ID not found") }
                return@launch
            }

            deleteBarUseCase(currentBarId)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, isDeleted = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, error = error.message) }
                }
        }
    }

    private fun saveBar() {
        _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentBarId = currentState.bar?.id

            if (currentBarId == null) {
                _uiState.update { it.copy(isSaving = false, error = "Bar ID not found") }
                return@launch
            }
            
            if (currentState.selectedLocation == null) {
                _uiState.update { it.copy(isSaving = false, error = "Please select a location") }
                return@launch
            }

            val request = SaveBarRequest(
                name = currentState.name,
                description = currentState.description,
                locationId = currentState.selectedLocation.id!!,
                address = currentState.address,
                latitude = currentState.latitude.toDoubleOrNull() ?: 0.0,
                longitude = currentState.longitude.toDoubleOrNull() ?: 0.0,
                locationBarUrl = currentState.locationBarUrl,
                machineIds = currentState.selectedMachineIds.map { it },
                status = currentState.bar?.status ?: "ACTIVE"
            )

            updateBarMachinesUseCase(currentBarId, request)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, error = error.message) }
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
    val selectedMachineIds: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null,
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val locationBarUrl: String = "",
    val selectedLocation: LocationModel? = null,
    val locations: List<LocationModel> = emptyList()
)

sealed interface EditBarEvent {
    data class OnNameChanged(val name: String) : EditBarEvent
    data class OnDescriptionChanged(val description: String) : EditBarEvent
    data class OnAddressChanged(val address: String) : EditBarEvent
    data class OnLatitudeChanged(val latitude: String) : EditBarEvent
    data class OnLongitudeChanged(val longitude: String) : EditBarEvent
    data class OnLocationBarUrlChanged(val url: String) : EditBarEvent
    data class OnLocationSelected(val locationId: String) : EditBarEvent
    data class ToggleMachineSelection(val machineId: String) : EditBarEvent
    data class RemoveMachineFromBar(val machineId: String) : EditBarEvent
    data object OnSaveTapped : EditBarEvent
    data object DeleteBar : EditBarEvent
}

