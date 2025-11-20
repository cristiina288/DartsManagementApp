package org.darts.dartsmanagement.ui.machines.newMachine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.darts.dartsmanagement.data.machines.requests.SaveMachineRequest
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.locations.model.LocationModel
import org.darts.dartsmanagement.domain.machines.SaveMachine
import org.darts.dartsmanagement.ui.home.machines.MachineState

class NewMachineViewModel(
    val getBars: GetBars,
    val saveMachine: SaveMachine,
) : ViewModel() {

    private val _bars = MutableStateFlow<List<BarModel>?>(null)
    val bars: StateFlow<List<BarModel>?> = _bars

    private val _locations = MutableStateFlow<List<LocationModel>?>(null)
    val locations: StateFlow<List<LocationModel>?> = _locations

    private val _machine = MutableStateFlow<MachineState>(MachineState())
    val machine: StateFlow<MachineState> = _machine


    init {
        getAllBars()
    }


    private fun getAllBars() {
        viewModelScope.launch {
            val result: List<BarModel> = withContext(Dispatchers.IO) {
                getBars()
            }

            _bars.value = result
        }
    }


    fun saveMachine() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                saveMachine(
                    saveMachineRequest = SaveMachineRequest(
                        name = machine.value.name ?: "",
                        counter = machine.value.counter,
                        barId = machine.value.barId ?: 0,
                    )
                )
            }
        }
    }


    fun saveName(name: String) {
        viewModelScope.launch {
            _machine.update { machine ->
                machine.copy(
                    name = name,
                )
            }
        }
    }


    fun saveCounter(counter: Int) {
        viewModelScope.launch {
            _machine.update { machine ->
                machine.copy(
                    counter = counter,
                )
            }
        }
    }


    fun saveBarId(barId: Int) {
        viewModelScope.launch {
            _machine.update { machine ->
                machine.copy(
                    barId = barId,
                )
            }
        }
    }
}

