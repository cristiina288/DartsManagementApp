package org.darts.dartsmanagement.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.characters.GetRandomCharacter
import org.darts.dartsmanagement.domain.collections.SaveCollection
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel

class CollectionsViewModel(
    val initialBarId: String? = null,
    val getRandomCharacter: GetRandomCharacter,
    val getBars: GetBars,
    val saveCollection: SaveCollection
) : ViewModel() {

    private val _state = MutableStateFlow<CollectionsState>(CollectionsState())
    val state: StateFlow<CollectionsState> = _state

    private val _bars = MutableStateFlow<List<BarModel?>?>(null)
    val bars: StateFlow<List<BarModel?>?> = _bars

    private val _collection = MutableStateFlow<CollectionsState>(CollectionsState())
    val collection: StateFlow<CollectionsState> = _collection


    init {
        getAllBars()
    }


    private fun getAllBars() {
        viewModelScope.launch {
            val result: List<BarModel> = withContext(Dispatchers.IO) {
                getBars()
            }

            _bars.value = result
            
            initialBarId?.let { barId ->
                onBarSelected(barId)
            }
        }
    }

    fun onBarSelected(barId: String) {
        val selectedBar = _bars.value?.find { it?.id == barId }
        val initialEntries = selectedBar?.machines?.map { machine ->
            MachineCollectionEntry(
                machineId = machine.id,
                counter = machine.counter,
                collectionAmounts = CollectionAmountsModel()
            )
        } ?: emptyList()

        _collection.update { collection ->
            collection.copy(
                barId = barId,
                machineEntries = initialEntries
            )
        }
    }

    fun onAddMachineEntry() {
        _collection.update { collection ->
            val newEntries = collection.machineEntries.toMutableList()
            newEntries.add(MachineCollectionEntry())
            collection.copy(machineEntries = newEntries)
        }
    }

    fun onRemoveMachineEntry(index: Int) {
        _collection.update { collection ->
            val newEntries = collection.machineEntries.toMutableList()
            if (newEntries.size > 1) {
                newEntries.removeAt(index)
            }
            collection.copy(machineEntries = newEntries)
        }
    }


    fun saveActualCollection() {
        viewModelScope.launch {
            try {
                val currentState = collection.value
                val barId = currentState.barId ?: return@launch
                val barName = bars.value?.find { it?.id == barId }?.name ?: "Unknown Bar"
                val comments = currentState.comments ?: ""
                val globalExtra = currentState.globalExtraPayment
                
                val machineFirestoreEntries = mutableListOf<org.darts.dartsmanagement.data.collections.CollectionMachineFirestore>()
                val machineCounters = mutableMapOf<String, Int>()

                var totalInitialBusinessAmount = 0.0
                var totalInitialBarAmount = 0.0

                currentState.machineEntries.forEachIndexed { index, entry ->
                    val machineIdInt = entry.machineId ?: return@forEachIndexed
                    val mIdString = machineIdInt.toString()
                    val initialAmounts = entry.collectionAmounts ?: CollectionAmountsModel()
                    
                    totalInitialBusinessAmount += initialAmounts.businessAmount
                    totalInitialBarAmount += initialAmounts.barAmount

                    val newCounter = (entry.counter ?: 0) + (entry.collectionAmounts?.totalCollection?.toInt() ?: 0)
                    machineCounters[mIdString] = newCounter
                    
                    // We record initial amounts per machine in the array
                    machineFirestoreEntries.add(
                        org.darts.dartsmanagement.data.collections.CollectionMachineFirestore(
                            machineId = machineIdInt,
                            barAmount = initialAmounts.barAmount,
                            businessAmount = initialAmounts.businessAmount,
                            totalCollection = initialAmounts.totalCollection
                        )
                    )
                }

                val finalTotalBusinessAmount = totalInitialBusinessAmount + globalExtra
                val finalTotalBarAmount = totalInitialBarAmount - globalExtra

                val success = withContext(Dispatchers.IO) {
                    saveCollection(
                        barId = barId,
                        barName = barName,
                        comments = comments,
                        totalBarAmount = finalTotalBarAmount,
                        totalBusinessAmount = finalTotalBusinessAmount,
                        machines = machineFirestoreEntries,
                        machineCounters = machineCounters
                    )
                }
                
                if (success) {
                    _collection.update { it.copy(snackbarMessage = "Guardado correctamente") }
                } else {
                    _collection.update { it.copy(snackbarMessage = "Error al guardar") }
                }
            } catch (e: Exception) {
                _collection.update { it.copy(snackbarMessage = "Error al guardar: ${e.message}") }
            }
        }
    }

    fun onSnackbarShown() {
        _collection.update { it.copy(snackbarMessage = null) }
    }

    fun onGlobalExtraPaymentChanged(extraAmount: Double) {
        _collection.update { it.copy(globalExtraPayment = extraAmount) }
    }

    fun saveCollectionAmounts(collectionAmounts: CollectionAmountsModel, index: Int) {
        _collection.update { collections ->
            val newEntries = collections.machineEntries.toMutableList()
            newEntries[index] = newEntries[index].copy(
                collectionAmounts = collectionAmounts
            )
            collections.copy(machineEntries = newEntries)
        }
    }


    fun saveCounterAndMachineIdCollection(counterCollection: Int?, machineId: Int?, index: Int) {
        _collection.update { collections ->
            val newEntries = collections.machineEntries.toMutableList()
            newEntries[index] = newEntries[index].copy(
                counter = counterCollection ?: 0,
                machineId = machineId ?: 0
            )
            collections.copy(machineEntries = newEntries)
        }
    }

    fun onCommentsChange(comments: String) {
        _collection.update { collection ->
            collection.copy(
                comments = comments
            )
        }
    }


    /*collectionAmounts = CollectionAmountsRequest(
                        totalCollection = 200,
                        barAmount = 80,
                        barPayment = 0,
                        businessAmount = 120,
                        extraAmount = 0
                    )*/
}