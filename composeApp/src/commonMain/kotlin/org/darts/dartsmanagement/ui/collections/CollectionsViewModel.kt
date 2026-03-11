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
import org.darts.dartsmanagement.data.auth.SessionManager
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.characters.GetRandomCharacter
import org.darts.dartsmanagement.domain.collections.SaveCollection
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel
import org.darts.dartsmanagement.domain.leagues.GetPendingLeaguesForBarUseCase
import org.darts.dartsmanagement.domain.leagues.SaveLeagueCollectionUseCase
import org.darts.dartsmanagement.domain.leagues.models.LeagueCollectionModel

class CollectionsViewModel(
    val initialBarId: String? = null,
    val getRandomCharacter: GetRandomCharacter,
    val getBars: GetBars,
    val saveCollection: SaveCollection,
    private val getPendingLeaguesForBarUseCase: GetPendingLeaguesForBarUseCase,
    private val saveLeagueCollectionUseCase: SaveLeagueCollectionUseCase,
    private val sessionManager: SessionManager
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
                collectionAmounts = CollectionAmountsModel(),
                contadorInput = machine.counter?.toString() ?: ""
            )
        } ?: emptyList()

        _collection.update { collection ->
            collection.copy(
                barId = barId,
                machineEntries = initialEntries,
                leaguePayments = emptyList(), // Reset league payments
                availableLeagues = emptyList()
            )
        }

        // Fetch pending leagues for this bar
        viewModelScope.launch {
            try {
                val pendingLeagues = withContext(Dispatchers.IO) {
                    getPendingLeaguesForBarUseCase(barId)
                }
                _collection.update { it.copy(availableLeagues = pendingLeagues) }
                
                // If there are pending leagues, add one initial entry if needed
                if (pendingLeagues.isNotEmpty()) {
                    onAddLeaguePaymentEntry()
                }
            } catch (e: Exception) {
                println("Error fetching pending leagues: $e")
            }
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

    fun onAddLeaguePaymentEntry() {
        _collection.update { collection ->
            val newPayments = collection.leaguePayments.toMutableList()
            newPayments.add(LeaguePaymentEntry())
            collection.copy(leaguePayments = newPayments)
        }
    }

    fun onRemoveLeaguePaymentEntry(index: Int) {
        _collection.update { collection ->
            val newPayments = collection.leaguePayments.toMutableList()
            newPayments.removeAt(index)
            collection.copy(leaguePayments = newPayments)
        }
    }

    fun onLeagueSelected(index: Int, leagueId: String) {
        _collection.update { collection ->
            val newPayments = collection.leaguePayments.toMutableList()
            val league = collection.availableLeagues.find { it.id == leagueId }
            val leagueName = league?.name ?: ""
            
            newPayments[index] = newPayments[index].copy(
                leagueId = leagueId, 
                leagueName = leagueName,
                error = null // Clear error on change
            )
            
            // Re-validate amount with new league context
            val currentAmount = newPayments[index].amount.toDoubleOrNull() ?: 0.0
            if (currentAmount > 0) {
                val pendingAmount = league?.bars?.find { it.barId == collection.barId }?.barFinances?.amountPending ?: 0.0
                if (currentAmount > pendingAmount) {
                    newPayments[index] = newPayments[index].copy(
                        error = "El precio no puede superar lo que le queda por pagar que es: $pendingAmount €"
                    )
                }
            }
            
            collection.copy(leaguePayments = newPayments)
        }
    }

    fun onLeagueAmountChanged(index: Int, amountStr: String) {
        _collection.update { collection ->
            val newPayments = collection.leaguePayments.toMutableList()
            val entry = newPayments[index]
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val pendingAmount = collection.availableLeagues
                .find { it.id == entry.leagueId }
                ?.bars?.find { it.barId == collection.barId }
                ?.barFinances?.amountPending ?: 0.0

            val error = if (amount > pendingAmount) {
                "El precio no puede superar lo que le queda por pagar que es: $pendingAmount €"
            } else null

            newPayments[index] = entry.copy(amount = amountStr, error = error)
            collection.copy(leaguePayments = newPayments)
        }
    }


    fun saveActualCollection(ignoreLeagueValidation: Boolean = false) {
        viewModelScope.launch {
            try {
                val currentState = collection.value
                
                // League Payment Validation
                if (!ignoreLeagueValidation) {
                    val emptyLeagueIndex = currentState.leaguePayments.indexOfFirst { 
                        it.leagueId.isNotEmpty() && (it.amount.isEmpty() || (it.amount.toDoubleOrNull() ?: 0.0) <= 0.0) 
                    }
                    if (emptyLeagueIndex != -1) {
                        _collection.update { it.copy(showLeagueValidationDialog = true, pendingLeagueIndex = emptyLeagueIndex) }
                        return@launch
                    }
                }

                val barId = currentState.barId ?: return@launch
                val barName = bars.value?.find { it?.id == barId }?.name ?: "Unknown Bar"
                val comments = currentState.comments ?: ""
                val globalExtra = currentState.globalExtraPayment
                
                val machineFirestoreEntries = mutableListOf<org.darts.dartsmanagement.data.collections.CollectionMachineFirestore>()
                val machineCounters = mutableMapOf<String, Int>()

                var totalInitialBusinessAmount = 0.0
                var totalInitialBarAmount = 0.0
                var totalCollection = 0.0

                currentState.machineEntries.forEachIndexed { index, entry ->
                    val machineIdInt = entry.machineId ?: return@forEachIndexed
                    val mIdString = machineIdInt.toString()
                    val initialAmounts = entry.collectionAmounts ?: CollectionAmountsModel()
                    
                    totalInitialBusinessAmount += initialAmounts.businessAmount
                    totalInitialBarAmount += initialAmounts.barAmount
                    totalCollection += initialAmounts.totalCollection

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

                val collectionId = withContext(Dispatchers.IO) {
                    saveCollection(
                        barId = barId,
                        barName = barName,
                        comments = comments,
                        totalBarAmount = finalTotalBarAmount,
                        totalBusinessAmount = finalTotalBusinessAmount,
                        totalCollection = totalCollection,
                        machines = machineFirestoreEntries,
                        machineCounters = machineCounters
                    )
                }
                
                if (collectionId != null) {
                    // If collection saved, save league payments
                    saveLeagueCollections(barId, collectionId)
                    _collection.update { it.copy(snackbarMessage = "Guardado correctamente", showLeagueValidationDialog = false) }
                } else {
                    _collection.update { it.copy(snackbarMessage = "Error al guardar") }
                }
            } catch (e: Exception) {
                _collection.update { it.copy(snackbarMessage = "Error al guardar: ${e.message}") }
            }
        }
    }

    private suspend fun saveLeagueCollections(barId: String, collectionId: String) {
        val currentState = collection.value
        val licenseId = sessionManager.licenseId.value ?: return
        val userId = sessionManager.userId.value ?: return

        currentState.leaguePayments.forEach { payment ->
            val amount = payment.amount.toDoubleOrNull() ?: 0.0
            if (payment.leagueId.isNotEmpty() && amount > 0.0) {
                val leagueCollection = LeagueCollectionModel(
                    licenseId = licenseId,
                    leagueId = payment.leagueId,
                    amount = amount,
                    collectionId = collectionId, // Link to collection
                    payeeId = barId,
                    method = "CASH_COLLECTION",
                    recordedBy = userId
                )
                withContext(Dispatchers.IO) {
                    saveLeagueCollectionUseCase(leagueCollection)
                }
            }
        }
    }

    fun onDismissLeagueValidation() {
        _collection.update { it.copy(showLeagueValidationDialog = false) }
    }

    fun onSnackbarShown() {
        _collection.update { it.copy(snackbarMessage = null) }
    }

    fun onGlobalExtraPaymentChanged(extraAmount: Double) {
        _collection.update { it.copy(globalExtraPayment = extraAmount) }
    }

    fun saveCollectionAmounts(totalCollectionStr: String, index: Int) {
        val totalCollection = totalCollectionStr.toDoubleOrNull() ?: 0.0
        
        _collection.update { collections ->
            val newEntries = collections.machineEntries.toMutableList()
            val oldCounter = newEntries[index].counter ?: 0
            
            val valueBarAmount = (totalCollection * 0.4)
            val valueBusinessAmount = (totalCollection * 0.6)
            
            val currentCounter = (oldCounter + totalCollection.toInt())

            newEntries[index] = newEntries[index].copy(
                recaudacionInput = totalCollectionStr,
                contadorInput = currentCounter.toString(),
                collectionAmounts = CollectionAmountsModel(
                    totalCollection = totalCollection,
                    barAmount = valueBarAmount,
                    businessAmount = valueBusinessAmount
                )
            )
            collections.copy(machineEntries = newEntries)
        }
    }


    fun saveCounterAndMachineIdCollection(counterCollection: Int?, machineId: String?, index: Int) {
        _collection.update { collections ->
            val newEntries = collections.machineEntries.toMutableList()
            newEntries[index] = newEntries[index].copy(
                counter = counterCollection ?: 0,
                machineId = machineId ?: "",
                contadorInput = counterCollection?.toString() ?: ""
            )
            collections.copy(machineEntries = newEntries)
        }
    }

    fun onInputModeChanged(mode: CollectionInputMode, index: Int) {
        _collection.update { collections ->
            val newEntries = collections.machineEntries.toMutableList()
            newEntries[index] = newEntries[index].copy(
                inputMode = mode
            )
            collections.copy(machineEntries = newEntries)
        }
    }

    fun onCurrentCounterChanged(currentCounterStr: String, index: Int) {
        val currentCounter = currentCounterStr.toIntOrNull()
        
        _collection.update { collections ->
            val newEntries = collections.machineEntries.toMutableList()
            val oldCounter = newEntries[index].counter ?: 0
            
            // If currentCounter is null (empty field), we treat it as 0 collection internally
            val totalCollection = if (currentCounter == null) 0.0 else (currentCounter - oldCounter).toDouble().coerceAtLeast(0.0)

            val valueBarAmount = (totalCollection * 0.4)
            val valueBusinessAmount = (totalCollection * 0.6)

            newEntries[index] = newEntries[index].copy(
                contadorInput = currentCounterStr,
                recaudacionInput = if (currentCounter == null || totalCollection == 0.0) "" else totalCollection.toString(),
                collectionAmounts = CollectionAmountsModel(
                    totalCollection = totalCollection,
                    barAmount = valueBarAmount,
                    businessAmount = valueBusinessAmount
                )
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
}