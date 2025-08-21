
// iosMain/kotlin/ExcelExporter.ios.kt
import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import platform.Foundation.*
import platform.UIKit.*

actual class ExcelExporter {

    actual suspend fun exportarAExcel(
        datos: List<MiObjeto>,
        nombreArchivo: String
    ): ExportResult = withContext(Dispatchers.Default) {
        try {
            // Crear contenido CSV (más simple para iOS)
            val csvContent = crearCSV(datos)

            // Guardar archivo
            val fileName = "${nombreArchivo}_${timestamp()}.csv"
            val filePath = guardarArchivo(csvContent, fileName)

            // Compartir archivo
            compartirArchivo(filePath)

            ExportResult.Success

        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Error desconocido")
        }
    }

    private fun crearCSV(datos: List<MiObjeto>): String {
        val sb = StringBuilder()

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

    private fun timestamp(): String {
        val formatter = NSDateFormatter()
        formatter.dateFormat = "yyyyMMdd_HHmmss"
        return formatter.stringFromDate(NSDate())
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun guardarArchivo(contenido: String, nombreArchivo: String): String {
        val documentsPath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).first() as String

        val filePath = "$documentsPath/$nombreArchivo"

        contenido.encodeToByteArray().usePinned { pinned ->
            val nsData = NSData.create(
                bytes = pinned.addressOf(0),
                length = contenido.encodeToByteArray().size.toULong()
            )
            nsData.writeToFile(filePath, true)
        }

        return filePath
    }

    private fun compartirArchivo(filePath: String) {
        val fileURL = NSURL.fileURLWithPath(filePath)
        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileURL),
            applicationActivities = null
        )

        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            activityViewController,
            animated = true,
            completion = null
        )
    }
}

actual object ExcelExporterFactory {
    actual fun create(): ExcelExporter {
        return ExcelExporter()
    }
}