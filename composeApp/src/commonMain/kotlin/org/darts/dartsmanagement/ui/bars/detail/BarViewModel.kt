package org.darts.dartsmanagement.ui.bars.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.domain.bars.DeleteBarUseCase
import org.darts.dartsmanagement.domain.bars.GetBar
import org.darts.dartsmanagement.domain.bars.models.BarModel

class BarViewModel(
    private val barId: String,
    private val getBar: GetBar,
    private val deleteBarUseCase: DeleteBarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BarScreenUiState())
    val uiState: StateFlow<BarScreenUiState> = _uiState.asStateFlow()

    init {
        loadBarDetails()
    }

    fun onEvent(event: BarEvent) {
        when (event) {
            BarEvent.DeleteBar -> deleteBar()
            BarEvent.Refresh -> loadBarDetails()
        }
    }

    private fun deleteBar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            deleteBarUseCase(barId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isDeleted = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun loadBarDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getBar(barId)
                .onSuccess { bar ->
                    _uiState.update {
                        it.copy(isLoading = false, bar = bar)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }
}

data class BarScreenUiState(
    val isLoading: Boolean = false,
    val bar: BarModel? = null,
    val error: String? = null,
    val isDeleted: Boolean = false
)

sealed interface BarEvent {
    data object DeleteBar : BarEvent
    data object Refresh : BarEvent
}
