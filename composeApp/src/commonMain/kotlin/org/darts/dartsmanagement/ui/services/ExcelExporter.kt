// commonMain/kotlin/ExcelExporter.kt
import kotlinx.datetime.LocalDateTime

// Tu clase de datos común
data class MiObjeto(
    val id: Int,
    val nombre: String,
    val fecha: LocalDateTime,
    val cantidad: Double,
    val activo: Boolean
)

// Resultado de la exportación
sealed class ExportResult {
    object Success : ExportResult()
    data class Error(val message: String) : ExportResult()
}

// Interface común
expect class ExcelExporter {
    suspend fun exportarAExcel(
        datos: List<MiObjeto>,
        nombreArchivo: String = "datos_exportados"
    ): ExportResult
}

// Factory para crear el exportador
expect object ExcelExporterFactory {
    fun create(): ExcelExporter
}