package org.darts.dartsmanagement.ui.locations.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.bars.models.BarModel

class LocationViewModel(
    private val locationId: String,
    private val getBars: GetBars
) : ViewModel() {

    private val _bars = MutableStateFlow<List<BarModel>>(emptyList())
    val bars: StateFlow<List<BarModel>> = _bars

    init {
        getBarsForLocation()
    }

    private fun getBarsForLocation() {
        viewModelScope.launch {
            val allBars = withContext(Dispatchers.IO) {
                getBars()
            }
            _bars.value = allBars.filter { bar ->
                bar.location.id == locationId
            }
        }
    }
}
