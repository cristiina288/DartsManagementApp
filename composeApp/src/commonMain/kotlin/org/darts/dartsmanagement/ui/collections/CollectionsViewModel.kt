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
        /*viewModelScope.launch {
            val result: CharacterModel = withContext(Dispatchers.IO) {
                getRandomCharacter()
            }

            _state.update { states ->
                states.copy(characterOfTheDay = result)
            }
        }*/

        getAllBars()
        
        initialBarId?.let { barId ->
            onBarSelected(barId)
        }
    }


    private fun getAllBars() {
        viewModelScope.launch {
            val result: List<BarModel> = withContext(Dispatchers.IO) {
                getBars()
            }

            _bars.value = result
        }
    }

    fun onBarSelected(barId: String) {
        _collection.update { collection ->
            collection.copy(
                barId = barId,
                machineEntries = listOf(MachineCollectionEntry())
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
                val comments = currentState.comments ?: ""
                val globalExtra = currentState.globalExtraPayment

                withContext(Dispatchers.IO) {
                    currentState.machineEntries.forEachIndexed { index, entry ->
                        val initialAmounts = entry.collectionAmounts ?: CollectionAmountsModel()
                        
                        // Apply global extra payment ONLY to the first machine to record it once
                        val machineExtra = if (index == 0) globalExtra else 0.0

                        val finalBusinessAmount = initialAmounts.businessAmount + machineExtra
                        val finalBarAmount = initialAmounts.barAmount - machineExtra

                        val finalCollectionAmounts = initialAmounts.copy(
                            businessAmount = finalBusinessAmount,
                            barAmount = finalBarAmount,
                            extraAmount = machineExtra
                        )

                        saveCollection(
                            collectionAmounts = finalCollectionAmounts,
                            newCounterMachine = (entry.counter ?: 0) + (entry.collectionAmounts?.totalCollection?.toInt() ?: 0),
                            machineId = entry.machineId ?: 0,
                            barId = barId,
                            comments = comments
                        )
                    }
                }
                _collection.update { it.copy(snackbarMessage = "Guardado correctamente") }
            } catch (e: Exception) {
                _collection.update { it.copy(snackbarMessage = "Error al guardar") }
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