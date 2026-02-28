package org.darts.dartsmanagement.ui.locations.detail

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
import org.darts.dartsmanagement.domain.locations.GetLocation
import org.darts.dartsmanagement.domain.locations.model.LocationModel

data class LocationUiState(
    val location: LocationModel? = null,
    val bars: List<BarModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LocationViewModel(
    private val locationId: String,
    private val getBars: GetBars,
    private val getLocation: GetLocation
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // 1. Get Location Details
            getLocation(locationId).onSuccess { location ->
                _uiState.update { it.copy(location = location) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }

            // 2. Get Bars for Location
            val allBars = withContext(Dispatchers.IO) {
                getBars()
            }
            val filteredBars = allBars.filter { bar ->
                bar.location.id == locationId
            }
            
            _uiState.update { it.copy(bars = filteredBars, isLoading = false) }
        }
    }
}
