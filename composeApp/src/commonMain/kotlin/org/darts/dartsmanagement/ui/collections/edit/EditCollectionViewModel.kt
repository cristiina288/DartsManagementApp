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
            comments = initialCollection.comments ?: ""
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onTotalCollectionChange(value: String) {
        _uiState.update { it.copy(totalCollection = value) }
    }

    fun onCommentsChange(value: String) {
        _uiState.update { it.copy(comments = value) }
    }

    fun save() {
        val currentState = _uiState.value
        val total = currentState.totalCollection.toDoubleOrNull() ?: 0.0
        
        if (total <= 0) {
            _uiState.update { it.copy(error = "La recaudación debe ser mayor a 0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Recalculate business and bar amounts (60/40 as per existing logic)
            // Note: If there was an extraAmount, we might want to preserve it or let the user edit it.
            // For now, I'll keep it simple and recalculate based on 60/40 + existing extraAmount
            val extra = initialCollection.extraAmount
            val business = total * 0.6 + extra
            val bar = total * 0.4 - extra

            val amounts = CollectionAmountsModel(
                totalCollection = total,
                barAmount = bar,
                businessAmount = business,
                extraAmount = extra,
                barPayment = 0.0 // Assuming not used or preserved
            )

            updateCollectionUseCase(initialCollection.id, amounts, currentState.comments)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
