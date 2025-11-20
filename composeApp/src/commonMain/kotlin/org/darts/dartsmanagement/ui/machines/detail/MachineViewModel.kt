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

class MachineViewModel(
    val getCollectionsByMachineId: GetCollectionsByMachineId,
    //val getBarById: GetBarById,
) : ViewModel() {

    private val _collections = MutableStateFlow<List<CollectionModel>?>(null)
    val collections: StateFlow<List<CollectionModel>?> = _collections


    init {
       // getCollectionsByMachine()
    }


    fun getCollectionsByMachine(machineId: Int) {
        viewModelScope.launch {
            val result: List<CollectionModel> = withContext(Dispatchers.IO) {
                getCollectionsByMachineId(machineId)
            }

            _collections.value = result
        }
    }
}

