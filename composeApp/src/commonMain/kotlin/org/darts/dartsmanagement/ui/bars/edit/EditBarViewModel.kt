package org.darts.dartsmanagement.ui.bars.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.machines.GetMachines
import org.darts.dartsmanagement.domain.machines.model.MachineModel

class EditBarViewModel(
    private val barModel: BarModel,
    private val getMachines: GetMachines
) : ViewModel() {

    private val _allMachines = MutableStateFlow<List<MachineModel>>(emptyList())
    val allMachines: StateFlow<List<MachineModel>> = _allMachines

    private val _selectedMachineIds = MutableStateFlow<MutableSet<Int>>(barModel.machines.mapNotNull { it.id }.toMutableSet())
    val selectedMachineIds: StateFlow<Set<Int>> = _selectedMachineIds

    init {
        getAllMachines()
    }

    private fun getAllMachines() {
        viewModelScope.launch {
            val machines = withContext(Dispatchers.IO) {
                getMachines()
            }
            _allMachines.value = machines
        }
    }

    fun toggleMachineSelection(machineId: Int) {
        _selectedMachineIds.update { currentSet ->
            val newSet = currentSet.toMutableSet()
            if (newSet.contains(machineId)) {
                newSet.remove(machineId)
            } else {
                newSet.add(machineId)
            }
            newSet
        }
    }

    fun updateSearchQuery(query: String) {
        // Implement search logic if needed, or filter in the UI
    }}
