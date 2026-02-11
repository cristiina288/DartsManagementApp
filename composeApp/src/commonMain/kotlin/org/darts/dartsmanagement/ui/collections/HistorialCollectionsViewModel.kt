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
import org.darts.dartsmanagement.domain.collections.GetCollectionsInDateRangeUseCase
import org.darts.dartsmanagement.domain.collections.GetPaginatedCollectionsUseCase
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.domain.machines.GetMachines
import org.darts.dartsmanagement.ui.home.ExportableCollection

class HistorialCollectionsViewModel(
    private val getPaginatedCollectionsUseCase: GetPaginatedCollectionsUseCase,
    private val getCollectionsInDateRangeUseCase: GetCollectionsInDateRangeUseCase,
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
            val excelExporter = ExcelExporterFactory.create() // Create exporter inside the function
            val endDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

            val collections = getCollectionsInDateRangeUseCase(fromDate, endDate)
            val allMachines = getMachines() // Get all machines
            val allBars = getBars() // Get all bars

            val machinesMap = allMachines.associateBy { it.id } // Map machineId to MachineModel
            val barsMap = allBars.associateBy { it.id } // Map barId to BarModel

            val exportableData = collections
                .mapNotNull { collection -> // Use mapNotNull to filter out collections without a valid machine
                    val machine = machinesMap[collection.machineId]
                    if (machine != null) {
                        val bar = barsMap[machine.barId] // Get bar from machine's barId
                        ExportableCollection(
                            barName = bar?.name,
                            machineId = collection.machineId,
                            machineName = machine.name,
                            businessAmount = collection.businessAmount,
                            barAmount = collection.barAmount,
                            totalCollections = collection.totalCollection,
                            createdAt = Instant.fromEpochSeconds(collection.createdAt)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .let { localDateTime ->
                                    "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/${localDateTime.monthNumber.toString().padStart(2, '0')}/${localDateTime.year}"
                                },
                            comments = collection.comments
                        )
                    } else {
                        null // Skip collections with no matching machine
                    }
                }
                .sortedBy { it.barName }
                .sortedBy { it.createdAt }
                .sortedBy { it.machineId }

            val headers = listOf(
                "Bar", "Id Máquina", "Máquina", "Empresa",
                "Bar", "Recaudación total", "Fecha", "Comentarios de la recaudación"
            )

            // Convert to List<List<Any>> for the Excel exporter if it expects that format
            val dataRows = exportableData.map {
                listOf(
                    it.barName ?: "",
                    it.machineId ?: "",
                    it.machineName ?: "",
                    it.businessAmount ?: 0.0,
                    it.barAmount ?: 0.0,
                    it.totalCollections ?: 0.0,
                    it.createdAt ?: "",
                    it.comments ?: ""
                )
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
}