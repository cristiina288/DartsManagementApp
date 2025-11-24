package org.darts.dartsmanagement.ui.locations.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.darts.dartsmanagement.domain.locations.GetLocations
import org.darts.dartsmanagement.domain.locations.model.LocationModel
import org.darts.dartsmanagement.domain.bars.GetBars

class LocationsListingViewModel(
    val getLocations: GetLocations,
    val getBars: GetBars
) : ViewModel() {

    private val _locations = MutableStateFlow<List<LocationModel?>?>(null)
    val locations: StateFlow<List<LocationModel?>?> = _locations


    init {
        getAllLocations()
    }


    private fun getAllLocations() {
        viewModelScope.launch {
            val allLocations = withContext(Dispatchers.IO) {
                getLocations()
            }
            val allBars = withContext(Dispatchers.IO) {
                getBars()
            }

            val locationsWithBars = allLocations.map { location ->
                location.copy(bars = allBars.filter { bar -> bar.location.id == location.id })
            }

            _locations.value = locationsWithBars
        }
    }
}

