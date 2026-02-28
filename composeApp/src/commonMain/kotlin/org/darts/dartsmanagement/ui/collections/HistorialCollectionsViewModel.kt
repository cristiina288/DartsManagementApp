package org.darts.dartsmanagement.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.collections.DeleteCollectionUseCase
import org.darts.dartsmanagement.domain.collections.GetCollectionsInDateRangeUseCase
import org.darts.dartsmanagement.domain.collections.GetPaginatedCollectionsUseCase
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.domain.machines.GetMachines

class HistorialCollectionsViewModel(
    private val getPaginatedCollectionsUseCase: GetPaginatedCollectionsUseCase,
    private val getCollectionsInDateRangeUseCase: GetCollectionsInDateRangeUseCase,
    private val deleteCollectionUseCase: DeleteCollectionUseCase,
    private val getBars: GetBars,
    private val getMachines: GetMachines
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialCollectionsUiState())
    val uiState: StateFlow<HistorialCollectionsUiState> = _uiState.asStateFlow()

    private val PAGE_SIZE = 10

    init {
        loadCollections(isInitialLoad = true)
    }

    fun onEvent(event: HistorialCollectionsEvent) {
        when (event) {
            is HistorialCollectionsEvent.LoadMoreCollections -> loadCollections()
            is HistorialCollectionsEvent.ExportCollections -> exportData(event.fromDate)
            is HistorialCollectionsEvent.DeleteCollection -> deleteCollection(event.collectionId)
        }
    }

    private fun deleteCollection(collectionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            deleteCollectionUseCase(collectionId).onSuccess {
                _uiState.update { state ->
                    state.copy(
                        collections = state.collections.filter { it.id != collectionId },
                        isLoading = false
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = "Error deleting collection: ${e.message}") }
            }
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


    fun exportData(fromDate: LocalDate) {
        viewModelScope.launch {
            val excelExporter = ExcelExporterFactory.create()
            val endDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

            val collections = getCollectionsInDateRangeUseCase(fromDate, endDate)
            val allMachines = getMachines()
            val allBars = getBars()

            val machinesMap = allMachines.associateBy { it.id }
            val barsMap = allBars.associateBy { it.id }

            // Grouping logic: Year-Month and MachineId
            val groupedData = collections
                .mapNotNull { collection ->
                    val machine = machinesMap[collection.machineId]
                    if (machine != null) {
                        val bar = barsMap[machine.barId]
                        val dateTime = Instant.fromEpochMilliseconds(collection.createdAt)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        
                        val monthKey = "${dateTime.year}-${dateTime.monthNumber}"
                        val machineKey = collection.machineId
                        
                        Triple(monthKey, machineKey, Pair(collection, machine to bar))
                    } else null
                }
                .groupBy { it.first } // Group by Month
                .mapValues { monthGroup ->
                    monthGroup.value.groupBy { it.second } // Group by Machine within Month
                }

            val headers = listOf(
                "Bar", "ID Maquina", "Maquina", "Fecha 1", "R1 Total",
                "Fecha 2", "R2 Total", "TOTAL R1+R2", "Porcentaje empresa",
                "Comentarios R1", "Comentarios R2"
            )

            val dataRows = mutableListOf<List<Any>>()

            groupedData.forEach { (_, machinesInMonth) ->
                machinesInMonth.forEach { (_, machineEntries) ->
                    // Sort collections by date within the month
                    val sortedEntries = machineEntries.map { it.third }.sortedBy { it.first.createdAt }
                    
                    val firstCollection = sortedEntries.getOrNull(0)?.first
                    val secondCollection = sortedEntries.getOrNull(1)?.first
                    val machineInfo = sortedEntries.first().second.first
                    val barInfo = sortedEntries.first().second.second

                    val r1Total = firstCollection?.totalCollection ?: 0.0
                    val r2Total = secondCollection?.totalCollection ?: 0.0
                    val totalR1R2 = r1Total + r2Total
                    val companyPercentage = totalR1R2 * 0.6

                    fun formatDate(timestamp: Long?): String {
                        if (timestamp == null) return ""
                        val dt = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
                        return "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')}/${dt.year}"
                    }

                    dataRows.add(
                        listOf(
                            barInfo?.name ?: "",
                            machineInfo.id ?: "",
                            machineInfo.name ?: "",
                            formatDate(firstCollection?.createdAt),
                            r1Total,
                            if (secondCollection != null) formatDate(secondCollection.createdAt) else "",
                            if (secondCollection != null) r2Total else "",
                            totalR1R2,
                            companyPercentage,
                            firstCollection?.comments ?: "",
                            secondCollection?.comments ?: ""
                        )
                    )
                }
            }

            when (val result = excelExporter.exportarAExcel(headers, dataRows, "reporte_recaudaciones_${fromDate.year}-${fromDate.monthNumber}_${endDate.year}-${endDate.monthNumber}")) {
                is ExportResult.Success -> {
                    println("Excel exportado correctamente")
                }
                is ExportResult.Error -> {
                    println("Error al exportar: ${result.message}")
                }
                else -> {}
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
    data object LoadMoreCollections : HistorialCollectionsEvent
    data class ExportCollections(val fromDate: LocalDate) : HistorialCollectionsEvent
    data class DeleteCollection(val collectionId: String) : HistorialCollectionsEvent
}