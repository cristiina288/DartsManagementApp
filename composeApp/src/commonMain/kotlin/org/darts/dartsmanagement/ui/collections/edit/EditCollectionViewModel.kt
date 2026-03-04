package org.darts.dartsmanagement.ui.collections.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.domain.collections.UpdateCollectionUseCase
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel
import org.darts.dartsmanagement.domain.collections.models.CollectionModel

data class EditCollectionUiState(
    val totalCollection: String = "",
    val totalBarAmount: String = "",
    val totalBusinessAmount: String = "",
    val comments: String = "",
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class EditCollectionViewModel(
    private val initialCollection: CollectionModel,
    private val updateCollectionUseCase: UpdateCollectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditCollectionUiState(
            totalCollection = initialCollection.totalCollection.toString(),
            totalBarAmount = initialCollection.totalBarAmount.toString(),
            totalBusinessAmount = initialCollection.totalBusinessAmount.toString(),
            comments = initialCollection.comments
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onTotalCollectionChange(value: String) {
        _uiState.update { it.copy(totalCollection = value) }
    }

    fun onTotalBarAmountChange(value: String) {
        _uiState.update { it.copy(totalBarAmount = value) }
    }

    fun onTotalBusinessAmountChange(value: String) {
        _uiState.update { it.copy(totalBusinessAmount = value) }
    }

    fun onCommentsChange(value: String) {
        _uiState.update { it.copy(comments = value) }
    }

    fun save() {
        val currentState = _uiState.value
        val total = currentState.totalCollection.toDoubleOrNull() ?: 0.0
        val bar = currentState.totalBarAmount.toDoubleOrNull() ?: 0.0
        val business = currentState.totalBusinessAmount.toDoubleOrNull() ?: 0.0
        
        if (total <= 0) {
            _uiState.update { it.copy(error = "La recaudación debe ser mayor a 0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val machines = initialCollection.machinesCollection.map {
                org.darts.dartsmanagement.data.collections.CollectionMachineFirestore(
                    machineId = it.machineId,
                    barAmount = it.barAmount,
                    businessAmount = it.businessAmount,
                    totalCollection = it.totalCollection
                )
            }

            updateCollectionUseCase(
                collectionId = initialCollection.id,
                comments = currentState.comments,
                totalBarAmount = bar,
                totalBusinessAmount = business,
                totalCollection = total,
                machines = machines
            )
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
