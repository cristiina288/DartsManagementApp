package org.darts.dartsmanagement.ui.home

import ExportResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant // Import Instant for conversion
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.darts.dartsmanagement.domain.auth.GetCurrentUser
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.collections.GetCollectionsInDateRangeUseCase // Import new use case
import org.darts.dartsmanagement.domain.machines.GetMachines // Import GetMachines
import org.darts.dartsmanagement.ui.auth.AuthViewModel
import org.darts.dartsmanagement.ui.collections.CollectionsState
import dev.gitlive.firebase.firestore.Timestamp // Import KMP Timestamp
import org.koin.core.component.getScopeName

sealed interface HomeEvent {
    data object Logout : HomeEvent
    data class ExportCollections(val fromDate: LocalDate) : HomeEvent
}

// Data class for export
data class ExportableCollection(

    val barName: String?,
    val machineId: Int?,
    val machineName: String?,
    val businessAmount: Double?,
    val barAmount: Double?,
    val totalCollections: Double?,
    val createdAt: String?, // Assuming String format for date
    val comments: String?
)

// Extension function to convert dev.gitlive.firebase.firestore.Timestamp to kotlinx.datetime.LocalDateTime
fun Timestamp.toKotlinxLocalDateTime(): kotlinx.datetime.LocalDateTime {
    return Instant.fromEpochSeconds(this.seconds, this.nanoseconds).toLocalDateTime(TimeZone.currentSystemDefault())
}


class HomeViewModel(
    val getCurrentUser: GetCurrentUser,
    private val authViewModel: AuthViewModel,
    private val getCollectionsInDateRangeUseCase: GetCollectionsInDateRangeUseCase, // New use case
    private val getBars: GetBars,
    private val getMachines: GetMachines // Inject GetMachines
) : ViewModel() {

    private val _collection = MutableStateFlow<CollectionsState>(CollectionsState())
    val collection: StateFlow<CollectionsState> = _collection

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _isCheckingAuth = MutableStateFlow(true)
    val isCheckingAuth: StateFlow<Boolean> = _isCheckingAuth


    init {
        checkCurrentUser()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.Logout -> {
                logout()
            }
            is HomeEvent.ExportCollections -> {
                exportData(event.fromDate)
            }
        }
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            _isCheckingAuth.value = true
            _currentUser.value = getCurrentUser()
            _isCheckingAuth.value = false
        }
    }

    private fun logout() {
        authViewModel.signOut()
        _currentUser.value = null
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