// commonMain/kotlin/ExcelExporter.kt
import kotlinx.datetime.LocalDateTime

// Resultado de la exportación
sealed class ExportResult {
    object Success : ExportResult()
    data class Error(val message: String) : ExportResult()
}

// Interface común
expect class ExcelExporter {
    suspend fun exportarAExcel(
        headers: List<String>,
        data: List<List<Any>>,
        nombreArchivo: String = "datos_exportados"
    ): ExportResult
}

// Factory para crear el exportador
expect object ExcelExporterFactory {
    fun create(): ExcelExporter
}