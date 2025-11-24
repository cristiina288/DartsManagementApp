package org.darts.dartsmanagement.ui.machines.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.darts.dartsmanagement.domain.collections.GetCollectionsByMachineId
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.bars.models.BarModel

class MachineViewModel(
    private val machineId: Int,
    private val getCollectionsByMachineId: GetCollectionsByMachineId,
    private val getBars: GetBars
) : ViewModel() {

    private val _collections = MutableStateFlow<List<CollectionModel>>(emptyList())
    val collections: StateFlow<List<CollectionModel>> = _collections

    private val _bar = MutableStateFlow<BarModel?>(null)
    val bar: StateFlow<BarModel?> = _bar

    init {
        getCollectionsByMachine()
        getBarForMachine()
    }

    private fun getCollectionsByMachine() {
        viewModelScope.launch {
            val result: List<CollectionModel> = withContext(Dispatchers.IO) {
                getCollectionsByMachineId(machineId)
            }
            _collections.value = result
        }
    }

    private fun getBarForMachine() {
        viewModelScope.launch {
            val allBars = withContext(Dispatchers.IO) {
                getBars()
            }
            // Find the bar whose machine_ids contains the current machineId
            _bar.value = allBars.firstOrNull { bar ->
                bar.machines.any { it.id == machineId }
            }
        }
    }
}

