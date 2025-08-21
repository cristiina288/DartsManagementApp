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
        datos: List<MiObjeto>,
        nombreArchivo: String
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            // Crear contenido CSV que Excel puede abrir
            val csvContent = crearCSVParaExcel(datos)

            // Guardar como archivo .csv en directorio interno
            val archivo = crearArchivo(nombreArchivo, "csv")
            FileWriter(archivo).use { writer ->
                writer.write(csvContent)
            }

            // Compartir archivo
            compartirArchivo(archivo)

            ExportResult.Success

        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Error desconocido")
        }
    }

    private fun crearCSVParaExcel(datos: List<MiObjeto>): String {
        val sb = StringBuilder()

        // BOM para UTF-8 (para que Excel lo reconozca correctamente)
        sb.append("\uFEFF")

        // Headers
        sb.appendLine("ID,Nombre,Fecha,Cantidad,Activo")

        // Datos
        datos.forEach { obj ->
            sb.appendLine("${obj.id},\"${obj.nombre}\",${formatearFecha(obj.fecha)},${obj.cantidad},${if (obj.activo) "Sí" else "No"}")
        }

        return sb.toString()
    }

    private fun formatearFecha(fecha: LocalDateTime): String {
        return "${fecha.dayOfMonth.toString().padStart(2, '0')}/" +
                "${fecha.monthNumber.toString().padStart(2, '0')}/" +
                "${fecha.year}"
    }

    // CAMBIO PRINCIPAL: Usar directorio interno en lugar de externo
    private fun crearArchivo(nombreArchivo: String, extension: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${nombreArchivo}_$timestamp.$extension"

        // Usar cache directory (interno) en lugar de external storage
        // Esto no requiere permisos especiales
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
                // Opcional: añadir un texto descriptivo
                putExtra(Intent.EXTRA_TEXT, "Archivo exportado desde Mi App")
            }

            // Añadir FLAG_ACTIVITY_NEW_TASK si el contexto no es una Activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(Intent.createChooser(intent, "Compartir archivo").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })

        } catch (e: Exception) {
            Log.e("ExcelExporter", "Error al compartir archivo: ${e.message}")
            // Opcionalmente puedes lanzar una excepción personalizada aquí
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