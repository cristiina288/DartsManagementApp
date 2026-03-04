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
            val excelExporter = ExcelExporterFactory.create()
            val endDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

            val collections = getCollectionsInDateRangeUseCase(fromDate, endDate)
            val allMachines = getMachines()
            val allBars = getBars()

            val machinesMap = allMachines.associateBy { it.id }
            val barsMap = allBars.associateBy { it.id }

            // Grouping logic: Year-Month and MachineId
            // One collection can have multiple machines, so we flatten them for the machine-based report
            val flattenedEntries = collections.flatMap { collection ->
                collection.machinesCollection.mapNotNull { machineEntry ->
                    val machine = machinesMap[machineEntry.machineId]
                    if (machine != null) {
                        val bar = barsMap[machine.barId]
                        val dateTime = Instant.fromEpochMilliseconds(collection.createdAt)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        
                        val monthKey = "${dateTime.year}-${dateTime.monthNumber}"
                        val machineKey = machineEntry.machineId
                        
                        // We store: collection-level data, machine-specific amounts, and machine/bar info
                        Triple(monthKey, machineKey, Triple(collection, machineEntry, machine to bar))
                    } else null
                }
            }

            val groupedData = flattenedEntries
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
                    
                    val firstEntry = sortedEntries.getOrNull(0)
                    val secondEntry = sortedEntries.getOrNull(1)

                    val firstCollection = firstEntry?.first
                    val firstMachineEntry = firstEntry?.second

                    val secondCollection = secondEntry?.first
                    val secondMachineEntry = secondEntry?.second

                    val machineInfo = firstEntry!!.third.first
                    val barInfo = firstEntry.third.second

                    val r1Total = firstMachineEntry?.totalCollection ?: 0.0
                    val r2Total = secondMachineEntry?.totalCollection ?: 0.0
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