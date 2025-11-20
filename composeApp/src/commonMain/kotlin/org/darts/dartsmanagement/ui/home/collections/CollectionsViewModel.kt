package org.darts.dartsmanagement.ui.home.collections

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


    fun saveActualCollection() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                saveCollection(
                    collectionAmounts = collection.value.collectionAmounts ?: CollectionAmountsModel(
                        totalCollection = 0.0,
                        barAmount = 0.0,
                        barPayment = 0.0,
                        businessAmount = 0.0,
                        extraAmount = 0.0
                    ),
                    newCounterMachine = (collection.value.counter ?: 0) + (collection.value.collectionAmounts?.totalCollection?.toInt() ?: 0),
                    machineId = collection.value.machineId ?: 0
                )
            }
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


    /*collectionAmounts = CollectionAmountsRequest(
                        totalCollection = 200,
                        barAmount = 80,
                        barPayment = 0,
                        businessAmount = 120,
                        extraAmount = 0
                    )*/
}