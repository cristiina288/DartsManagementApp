// androidMain/kotlin/ExcelExporter.android.kt
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*


actual class ExcelExporter(private val context: Context) {

    actual suspend fun exportarAExcel(
        headers: List<String>,
        data: List<List<Any>>,
        nombreArchivo: String
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val csvContent = crearCSVParaExcel(headers, data)

            val archivo = crearArchivo(nombreArchivo, "csv")
            FileWriter(archivo).use { writer ->
                writer.write(csvContent)
            }

            compartirArchivo(archivo)

            ExportResult.Success

        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Error desconocido")
        }
    }

    private fun crearCSVParaExcel(headers: List<String>, data: List<List<Any>>): String {
        val sb = StringBuilder()

        sb.append("\uFEFF") // BOM para UTF-8

        // Headers
        sb.appendLine(headers.joinToString(separator = ","))

        // Data
        data.forEach { row ->
            sb.appendLine(row.joinToString(separator = ",") { value ->
                // Basic CSV escaping for values that might contain commas or quotes
                "\"${value.toString().replace("\"", "\"\"")}\""
            })
        }

        return sb.toString()
    }

    private fun formatearFecha(fecha: LocalDateTime): String {
        return "${fecha.dayOfMonth.toString().padStart(2, '0')}/" +
                "${fecha.monthNumber.toString().padStart(2, '0')}/" +
                "${fecha.year}"
    }

    private fun crearArchivo(nombreArchivo: String, extension: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${nombreArchivo}_$timestamp.$extension"

        return File(context.cacheDir, fileName)
    }

    private fun compartirArchivo(archivo: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivo
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_TEXT, "Archivo exportado desde Darts Management")
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(Intent.createChooser(intent, "Compartir archivo").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })

        } catch (e: Exception) {
            Log.e("ExcelExporter", "Error al compartir archivo: ${e.message}")
        }
    }
}

actual object ExcelExporterFactory {
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    actual fun create(): ExcelExporter {
        return ExcelExporter(appContext)
    }
}