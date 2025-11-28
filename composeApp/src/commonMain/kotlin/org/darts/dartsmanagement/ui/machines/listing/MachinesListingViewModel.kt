package org.darts.dartsmanagement.ui.machines.listing

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
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.ui.machines.listing.MachinesUiModel

class MachinesListingViewModel(
    val getMachines: GetMachines,
    val getBars: GetBars
) : ViewModel() {

    private val _machinesUiModel = MutableStateFlow<List<MachinesUiModel>>(emptyList())
    val machinesUiModel: StateFlow<List<MachinesUiModel>> = _machinesUiModel


    init {
        loadMachines()
    }


    fun loadMachines() {
        viewModelScope.launch {
            val allMachines = withContext(Dispatchers.IO) {
                getMachines()
            }
            val allBars = withContext(Dispatchers.IO) {
                getBars()
            }

            _machinesUiModel.value = allMachines.map { machine ->
                val bar = allBars.firstOrNull { it.id == machine.barId }
                MachinesUiModel(
                    machine = machine,
                    barName = bar?.name
                )
            }
        }
    }
}

