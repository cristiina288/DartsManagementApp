package org.darts.dartsmanagement.ui.bars.newBar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.data.bars.requests.SaveBarRequest
import org.darts.dartsmanagement.domain.bars.SaveBar
import org.darts.dartsmanagement.domain.locations.GetLocations
import org.darts.dartsmanagement.domain.machines.GetMachines

sealed interface NewBarEvent {
    data class OnNameChanged(val name: String) : NewBarEvent
    data class OnDescriptionChanged(val description: String) : NewBarEvent
    data class OnAddressChanged(val address: String) : NewBarEvent
    data class OnLatitudeChanged(val latitude: String) : NewBarEvent
    data class OnLongitudeChanged(val longitude: String) : NewBarEvent
    data class OnLocationBarUrlChanged(val url: String) : NewBarEvent
    data class OnLocationSelected(val locationId: String) : NewBarEvent
    data class ToggleMachineSelection(val machineId: Int) : NewBarEvent
    data object OnSaveTapped : NewBarEvent
}

class NewBarViewModel(
    private val getMachines: GetMachines,
    private val getLocations: GetLocations,
    private val saveBar: SaveBar
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewBarUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val locations = getLocations()
                val machines = getMachines()
                _uiState.update {
                    it.copy(
                        locations = locations,
                        allMachines = machines,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onEvent(event: NewBarEvent) {
        when (event) {
            is NewBarEvent.OnNameChanged -> _uiState.update { it.copy(name = event.name) }
            is NewBarEvent.OnDescriptionChanged -> _uiState.update { it.copy(description = event.description) }
            is NewBarEvent.OnAddressChanged -> _uiState.update { it.copy(address = event.address) }
            is NewBarEvent.OnLatitudeChanged -> _uiState.update { it.copy(latitude = event.latitude) }
            is NewBarEvent.OnLongitudeChanged -> _uiState.update { it.copy(longitude = event.longitude) }
            is NewBarEvent.OnLocationBarUrlChanged -> _uiState.update { it.copy(locationBarUrl = event.url) }
            is NewBarEvent.OnLocationSelected -> {
                val location = _uiState.value.locations.find { it.id == event.locationId }
                _uiState.update { it.copy(selectedLocation = location) }
            }
            is NewBarEvent.ToggleMachineSelection -> {
                val selectedIds = _uiState.value.selectedMachineIds
                if (event.machineId in selectedIds) {
                    selectedIds.remove(event.machineId)
                } else {
                    selectedIds.add(event.machineId)
                }
                val assignedMachines = _uiState.value.allMachines.filter { it.id in selectedIds }
                _uiState.update { it.copy(selectedMachineIds = selectedIds, assignedMachines = assignedMachines) }
            }
            is NewBarEvent.OnSaveTapped -> saveBar()
        }
    }

    private fun saveBar() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val currentState = _uiState.value
            if (currentState.selectedLocation == null) {
                _uiState.update { it.copy(isLoading = false, error = "Please select a location") }
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
                machineIds = currentState.selectedMachineIds.map { it.toLong() },
                //id = 0,
                statusId = 1
            )

            try {
                saveBar(request)
                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

