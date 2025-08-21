package org.darts.dartsmanagement.ui.home

import ExcelExporter
import MiObjeto
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.characters.GetRandomCharacter
import org.darts.dartsmanagement.domain.collections.SaveCollection
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel
import org.darts.dartsmanagement.ui.home.collections.CollectionsState

class HomeViewModel(
    //val saveCollection: SaveCollection
) : ViewModel() {

    private val _collection = MutableStateFlow<CollectionsState>(CollectionsState())
    val collection: StateFlow<CollectionsState> = _collection


    init {
        /*viewModelScope.launch {
            val result: CharacterModel = withContext(Dispatchers.IO) {
                getRandomCharacter()
            }

            _state.update { states ->
                states.copy(characterOfTheDay = result)
            }
        }*/

        //exportData()
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