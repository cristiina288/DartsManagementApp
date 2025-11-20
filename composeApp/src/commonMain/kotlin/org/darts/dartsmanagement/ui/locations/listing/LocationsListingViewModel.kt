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

class LocationsListingViewModel(
    val getLocations: GetLocations,
) : ViewModel() {

    private val _locations = MutableStateFlow<List<LocationModel?>?>(null)
    val locations: StateFlow<List<LocationModel?>?> = _locations


    init {
        getAllLocations()
    }


    private fun getAllLocations() {
        viewModelScope.launch {
            val result: List<LocationModel> = withContext(Dispatchers.IO) {
                getLocations()
            }

            _locations.value = result
        }
    }
}

