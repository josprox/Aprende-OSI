package com.josprox.redesosi.vm

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.josprox.redesosi.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.system.exitProcess

// Estado de la UI para mostrar mensajes (cargando, éxito, error)
data class BackupUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

class BackupViewModel(
    private val application: Application,
    private val db: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    // Este es el nombre de tu BD definido en AppDatabase.kt
    private val DATABASE_NAME = "study_app_database"

    fun createBackup(context: Context, targetUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, message = "Creando copia de seguridad...", isError = false) }
            try {
                // 1. ¡CRÍTICO! Cerrar la BD antes de copiarla.
                db.close()

                // 2. Obtener el archivo de la BD interna
                val dbFile = context.getDatabasePath(DATABASE_NAME)

                // 3. Abrir streams (entrada: BD, salida: archivo elegido por el usuario)
                FileInputStream(dbFile).use { inputStream ->
                    context.contentResolver.openOutputStream(targetUri).use { outputStream ->
                        if (outputStream == null) throw Exception("No se pudo abrir el archivo de destino")
                        // 4. Copiar los bytes
                        inputStream.copyTo(outputStream)
                    }
                }
                _uiState.update { it.copy(isLoading = false, message = "Copia de seguridad creada con éxito.", isError = false) }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, message = "Error al crear copia de seguridad: ${e.message}", isError = true) }
            }
            // 5. La BD se reabrirá automáticamente en la próxima llamada a getDatabase()
        }
    }

    fun restoreBackup(context: Context, sourceUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, message = "Restaurando datos...", isError = false) }
            try {
                // 1. Obtener la ruta del archivo de la BD interna
                val dbFile = context.getDatabasePath(DATABASE_NAME)

                // 2. ¡CRÍTICO! Cerrar la conexión actual a la BD
                db.close()

                // 3. Abrir streams (entrada: backup, salida: BD interna)
                context.contentResolver.openInputStream(sourceUri).use { inputStream ->
                    if (inputStream == null) throw Exception("No se pudo abrir el archivo de backup")
                    // 4. Copiar bytes (SOBREESCRIBIENDO la BD actual)
                    FileOutputStream(dbFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                val successMessage = "¡Datos restaurados con éxito! Reiniciando la aplicación..."
                _uiState.update { it.copy(isLoading = false, message = successMessage, isError = false) }

                // Mostrar Toast y luego reiniciar
                launch(Dispatchers.Main) {
                    Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show()

                    // --- LÓGICA DE REINICIO AUTOMÁTICO ---

                    // 1. Crear el intent para relanzar la actividad principal de la app
                    val packageManager = context.packageManager
                    val intent = packageManager.getLaunchIntentForPackage(context.packageName)

                    // Asegurar que el intent no sea nulo antes de lanzarlo
                    intent?.let {
                        val componentName = it.component
                        val mainIntent = Intent.makeRestartActivityTask(componentName)
                        context.startActivity(mainIntent)

                        // 2. Terminar el proceso actual
                        exitProcess(0)
                    } ?: run {
                        // Caso de fallback si no se encuentra el intent
                        Toast.makeText(context, "Error al intentar reiniciar. Por favor, reinicia manualmente.", Toast.LENGTH_LONG).show()
                    }
                    // ------------------------------------
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, message = "Error al restaurar: ${e.message}", isError = true) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

/**
 * Necesitamos un Factory para poder pasar 'Application' y 'AppDatabase'
 * al constructor del ViewModel.
 */
class BackupViewModelFactory(
    private val application: Application,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BackupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BackupViewModel(application, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}