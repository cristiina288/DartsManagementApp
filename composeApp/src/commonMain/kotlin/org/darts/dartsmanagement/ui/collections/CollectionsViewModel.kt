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
                machineId = null
            )
        }
    }


    fun saveActualCollection() {
        viewModelScope.launch {
            try {
                val initialAmounts = collection.value.collectionAmounts ?: CollectionAmountsModel()
                val extraAmount = initialAmounts.extraAmount

                val finalBusinessAmount = initialAmounts.businessAmount + extraAmount
                val finalBarAmount = initialAmounts.barAmount - extraAmount

                val finalCollectionAmounts = initialAmounts.copy(
                    businessAmount = finalBusinessAmount,
                    barAmount = finalBarAmount
                )

                withContext(Dispatchers.IO) {
                    saveCollection(
                        collectionAmounts = finalCollectionAmounts,
                        newCounterMachine = (collection.value.counter ?: 0) + (collection.value.collectionAmounts?.totalCollection?.toInt() ?: 0),
                        machineId = collection.value.machineId ?: 0,
                        barId = collection.value.barId ?: "", // Keep barId
                        comments = collection.value.comments ?: ""
                    )
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

    fun onExtraPaymentChanged(extraAmount: Double) {
        _collection.update { currentState ->
            val currentAmounts = currentState.collectionAmounts ?: CollectionAmountsModel()
            currentState.copy(
                collectionAmounts = currentAmounts.copy(extraAmount = extraAmount)
            )
        }
    }

    fun saveCollectionAmounts(collection: CollectionAmountsModel) {
        viewModelScope.launch {
            _collection.update { collections ->
                collections.copy(
                    collectionAmounts = collection,
                )
            }
        }
    }


    fun saveCounterAndMachineIdCollection(counterCollection: Int?, machineId: Int?) {
        viewModelScope.launch {
            _collection.update { collections ->
                collections.copy(
                    counter = (counterCollection ?: 0),
                    machineId = (machineId ?: 0)
                )
            }
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