package org.darts.dartsmanagement.ui.home

import ExcelExporter
import ExportResult
import MiObjeto
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.darts.dartsmanagement.domain.auth.GetCurrentUser
import org.darts.dartsmanagement.ui.collections.CollectionsState

import org.darts.dartsmanagement.ui.auth.AuthViewModel

sealed interface HomeEvent {
    data object Logout : HomeEvent
}

class HomeViewModel(
    val getCurrentUser: GetCurrentUser,
    private val authViewModel: AuthViewModel
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
           /* val result: List<BarModel> = withContext(Dispatchers.IO) {
                getBars()
            }

            _bars.value = result*/
            val misDatos = listOf(
                MiObjeto(
                    id = 1,
                    nombre = "Producto A",
                    fecha = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    cantidad = 100.50,
                    activo = true
                ),
                MiObjeto(
                    id = 2,
                    nombre = "Producto B",
                    fecha = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    cantidad = 75.25,
                    activo = false
                ),
                MiObjeto(
                    id = 3,
                    nombre = "Producto C",
                    fecha = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    cantidad = 200.00,
                    activo = true
                )
            )

            // Exportar
            when (val result = excelExporter.exportarAExcel(misDatos, "mi_reporte")) {
                is ExportResult.Success -> {
                    // Mostrar mensaje de éxito
                    println("Excel exportado correctamente")
                }
                is ExportResult.Error -> {
                    // Manejar error
                    println("Error al exportar: ${result.message}")
                }

                else -> {}
            }

        }
    }
/*



    fun saveActualCollection() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                saveCollection(
                    collectionAmounts = collection.value.collectionAmounts ?: CollectionAmountsModel(
                        totalCollection = 0,
                        barAmount = 0,
                        barPayment = 0,
                        businessAmount = 0,
                        extraAmount = 0
                    ),
                    newCounterMachine = (collection.value.counter ?: 0) + (collection.value.collectionAmounts?.totalCollection ?: 0)
                )
            }
        }
    }
*/

}