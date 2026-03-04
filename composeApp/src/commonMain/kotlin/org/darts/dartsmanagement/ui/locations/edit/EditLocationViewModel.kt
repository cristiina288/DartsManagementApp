package org.darts.dartsmanagement.ui.locations.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.data.locations.requests.SaveLocationRequest
import org.darts.dartsmanagement.domain.locations.UpdateLocation
import org.darts.dartsmanagement.domain.locations.model.LocationModel

sealed interface EditLocationEvent {
    data class OnNameChanged(val name: String) : EditLocationEvent
    data class OnPostalCodeChanged(val postalCode: String) : EditLocationEvent
    data class OnProvinceChanged(val province: String) : EditLocationEvent
    data object OnSaveTapped : EditLocationEvent
}

data class EditLocationUiState(
    val name: String = "",
    val postalCode: String = "",
    val province: String = "",
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class EditLocationViewModel(
    private val initialLocation: LocationModel,
    private val updateLocation: UpdateLocation
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditLocationUiState(
            name = initialLocation.name.orEmpty(),
            postalCode = initialLocation.postalCode.orEmpty(),
            province = initialLocation.province.orEmpty()
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: EditLocationEvent) {
        when (event) {
            is EditLocationEvent.OnNameChanged -> _uiState.update { it.copy(name = event.name) }
            is EditLocationEvent.OnPostalCodeChanged -> _uiState.update { it.copy(postalCode = event.postalCode) }
            is EditLocationEvent.OnProvinceChanged -> _uiState.update { it.copy(province = event.province) }
            is EditLocationEvent.OnSaveTapped -> saveLocation()
        }
    }

    private fun saveLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val currentState = _uiState.value

            val request = SaveLocationRequest(
                name = currentState.name,
                postalCode = currentState.postalCode,
                province = currentState.province
            )

            val locationId = initialLocation.id
            if (locationId == null) {
                _uiState.update { it.copy(isLoading = false, error = "ID de ubicación no encontrado") }
                return@launch
            }

            updateLocation(locationId, request)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
