package org.darts.dartsmanagement.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.darts.dartsmanagement.domain.collections.GetPaginatedCollectionsUseCase
import org.darts.dartsmanagement.domain.collections.models.CollectionModel

class HistorialCollectionsViewModel(
    private val getPaginatedCollectionsUseCase: GetPaginatedCollectionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialCollectionsUiState())
    val uiState: StateFlow<HistorialCollectionsUiState> = _uiState.asStateFlow()

    private val PAGE_SIZE = 10

    init {
        loadCollections(isInitialLoad = true)
    }

    fun onEvent(event: HistorialCollectionsEvent) {
        when (event) {
            HistorialCollectionsEvent.LoadMoreCollections -> loadCollections()
        }
    }

    private fun loadCollections(isInitialLoad: Boolean = false) {
        if (_uiState.value.isLoading || !_uiState.value.canLoadMore) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val lastCollection = _uiState.value.collections.lastOrNull()
                val lastCollectionCreatedAt = lastCollection?.createdAt
                val lastCollectionDocumentId = lastCollection?.id
                val newCollections = getPaginatedCollectionsUseCase(lastCollectionCreatedAt, lastCollectionDocumentId, PAGE_SIZE)

                _uiState.update {
                    it.copy(
                        collections = if (isInitialLoad) newCollections else it.collections + newCollections,
                        isLoading = false,
                        canLoadMore = newCollections.size == PAGE_SIZE
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error loading collections: ${e.message}"
                    )
                }
                println("Error loading collections: $e")
            }
        }
    }
}

data class HistorialCollectionsUiState(
    val collections: List<CollectionModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val canLoadMore: Boolean = true
)

sealed interface HistorialCollectionsEvent {
    object LoadMoreCollections : HistorialCollectionsEvent
}