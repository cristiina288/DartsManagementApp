package org.darts.dartsmanagement.ui.home.bars.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.darts.dartsmanagement.domain.machines.GetMachines
import org.darts.dartsmanagement.domain.machines.model.MachineModel

class BarViewModel(
    val getMachines: GetMachines,
) : ViewModel() {

    private val _machines = MutableStateFlow<List<MachineModel>?>(null)
    val machines: StateFlow<List<MachineModel>?> = _machines


    init {
        getAllMachines()
    }


    private fun getAllMachines() {
        viewModelScope.launch {
            val result: List<MachineModel> = withContext(Dispatchers.IO) {
                getMachines()
            }

            _machines.value = result
        }
    }
}

