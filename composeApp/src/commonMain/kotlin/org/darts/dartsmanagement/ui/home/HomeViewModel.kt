package org.darts.dartsmanagement.ui.home

import ExcelExporter
import ExportResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant // Import Instant for conversion
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.darts.dartsmanagement.domain.auth.GetCurrentUser
import org.darts.dartsmanagement.ui.collections.CollectionsState
import org.darts.dartsmanagement.domain.collections.GetCollectionsForMonth
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.machines.GetMachines // Import GetMachines
import org.darts.dartsmanagement.ui.auth.AuthViewModel
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.domain.machines.model.MachineModel // Import MachineModel
import dev.gitlive.firebase.firestore.Timestamp // Import KMP Timestamp
import kotlinx.datetime.toInstant // Import toInstant for conversion

sealed interface HomeEvent {
    data object Logout : HomeEvent
}

// Data class for export
data class ExportableCollection(
    val barId: String?,
    val barName: String?,
    val machineId: Int?,
    val businessAmount: Double?,
    val barAmount: Double?,
    val totalCollections: Double?,
    //val createdAt: String? // Assuming String format for date
)

// Extension function to convert dev.gitlive.firebase.firestore.Timestamp to kotlinx.datetime.LocalDateTime
fun Timestamp.toKotlinxLocalDateTime(): kotlinx.datetime.LocalDateTime {
    return Instant.fromEpochSeconds(this.seconds, this.nanoseconds).toLocalDateTime(TimeZone.currentSystemDefault())
}


class HomeViewModel(
    val getCurrentUser: GetCurrentUser,
    private val authViewModel: AuthViewModel,
    private val getCollectionsForMonth: GetCollectionsForMonth,
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

    fun exportData(excelExporter: ExcelExporter) {
        viewModelScope.launch {
            val currentMoment = Clock.System.now()
            val localDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
            val currentYear = localDateTime.year
            val currentMonth = localDateTime.monthNumber

            val collections = getCollectionsForMonth(currentYear, currentMonth)
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
                            barId = machine.barId, // Use barId from MachineModel
                            barName = bar?.name, // Get bar name from BarModel
                            machineId = collection.machineId,
                            businessAmount = collection.collectionAmounts?.businessAmount,
                            barAmount = collection.collectionAmounts?.barAmount,
                            totalCollections = collection.collectionAmounts?.totalCollection,
                            //createdAt = collection.createdAt?.toKotlinxLocalDateTime()?.toString()
                        )
                    } else {
                        null // Skip collections with no matching machine
                    }
                }
                .sortedBy { it.barId } // Sort by barId after enrichment

            val headers = listOf(
                "barId", "barName", "machineId", "businessAmount",
                "barAmount", "totalCollections"//, "createdAt"
            )

            // Convert to List<List<Any>> for the Excel exporter if it expects that format
            val dataRows = exportableData.map {
                listOf(
                    it.barId ?: "",
                    it.barName ?: "",
                    it.machineId ?: "",
                    it.businessAmount ?: 0.0,
                    it.barAmount ?: 0.0,
                    it.totalCollections ?: 0.0,
                    //it.createdAt ?: ""
                )
            }


            when (val result = excelExporter.exportarAExcel(headers, dataRows, "reporte_recaudaciones_${currentMonth}_${currentYear}")) {
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