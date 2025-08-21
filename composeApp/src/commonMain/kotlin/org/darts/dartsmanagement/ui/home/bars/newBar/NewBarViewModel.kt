package org.darts.dartsmanagement.ui.home.bars.newBar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.darts.dartsmanagement.data.bars.requests.SaveBarRequest
import org.darts.dartsmanagement.domain.bars.SaveBar
import org.darts.dartsmanagement.domain.locations.GetLocations
import org.darts.dartsmanagement.domain.locations.model.LocationModel
import org.darts.dartsmanagement.domain.machines.GetMachines
import org.darts.dartsmanagement.domain.machines.model.MachineModel
import org.darts.dartsmanagement.ui.home.bars.BarState

class NewBarViewModel(
    val getMachines: GetMachines,
    val getLocations: GetLocations,
    val saveBar: SaveBar
) : ViewModel() {

    private val _machines = MutableStateFlow<List<MachineModel>?>(null)
    val machines: StateFlow<List<MachineModel>?> = _machines

    private val _locations = MutableStateFlow<List<LocationModel>?>(null)
    val locations: StateFlow<List<LocationModel>?> = _locations

    private val _bar = MutableStateFlow<BarState>(BarState())
    val bar: StateFlow<BarState> = _bar


    init {
        getAllMachines()
        getAllLocations()
    }


    private fun getAllMachines() {
        viewModelScope.launch {
            val result: List<MachineModel> = withContext(Dispatchers.IO) {
                getMachines()
            }

            _machines.value = result
        }
    }


    private fun getAllLocations() {
        viewModelScope.launch {
            val result: List<LocationModel> = withContext(Dispatchers.IO) {
                getLocations()
            }

            _locations.value = result
        }
    }


    fun saveBar() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                saveBar(
                    saveBarRequest = SaveBarRequest(
                        name = bar.value.name ?: "",
                        description = bar.value.description,
                        machineId = bar.value.machineId,
                        locationId = bar.value.locationId,
                        locationBarUrl = bar.value.locationBarUrl,

                    )
                )
            }
        }
    }


    fun saveName(name: String) {
        viewModelScope.launch {
            _bar.update { bar ->
                bar.copy(
                    name = name,
                )
            }
        }
    }


    fun saveLocationId(locationId: Int) {
        viewModelScope.launch {
            _bar.update { bar ->
                bar.copy(
                    locationId = locationId,
                )
            }
        }
    }


    fun saveMapLink(locationBarUrl: String) {
        viewModelScope.launch {
            _bar.update { bar ->
                bar.copy(
                    locationBarUrl = locationBarUrl,
                )
            }
        }
    }

    fun saveMachineId(machineId: Int) {
        viewModelScope.launch {
            _bar.update { bar ->
                bar.copy(
                    machineId = machineId,
                )
            }
        }
    }

    fun saveDescription(description: String) {
        viewModelScope.launch {
            _bar.update { bar ->
                bar.copy(
                    description = description,
                )
            }
        }
    }
}

