package org.darts.dartsmanagement.ui.locations.newLocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.data.locations.requests.SaveLocationRequest
import org.darts.dartsmanagement.domain.locations.SaveLocation

sealed interface NewLocationEvent {
    data class OnNameChanged(val name: String) : NewLocationEvent
    data class OnPostalCodeChanged(val postalCode: String) : NewLocationEvent
    data object OnSaveTapped : NewLocationEvent
}

class NewLocationViewModel(
    private val saveLocation: SaveLocation
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewLocationUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: NewLocationEvent) {
        when (event) {
            is NewLocationEvent.OnNameChanged -> _uiState.update { it.copy(name = event.name) }
            is NewLocationEvent.OnPostalCodeChanged -> _uiState.update { it.copy(postalCode = event.postalCode) }
            is NewLocationEvent.OnSaveTapped -> saveLocation()
        }
    }

    private fun saveLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val currentState = _uiState.value

            val request = SaveLocationRequest(
                name = currentState.name,
                postalCode = currentState.postalCode
            )

            try {
                saveLocation(request)
                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}