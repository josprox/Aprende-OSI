package com.josprox.redesosi.vm

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josprox.redesosi.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import com.josprox.redesosi.data.database.SubjectEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// --- ELIMINAMOS FileAction DE AQUÍ ---

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val repository: StudyRepository,
    private val application: Application // Hilt inyectará el contexto
) : ViewModel() {

    // --- AÑADIDO: FileAction AHORA ESTÁ DENTRO DE LA CLASE ---
    enum class FileAction {
        IMPORT, // Añadir nueva materia
        UPDATE  // Actualizar materia existente
    }
    // --- FIN MOVIMIENTO ---

    val subjects = repository.getAllSubjects()

    // --- Lógica para diálogos ---
    // 1. Guarda la materia seleccionada en la pulsación larga
    private val _selectedSubject = MutableStateFlow<SubjectEntity?>(null)
    val selectedSubject = _selectedSubject.asStateFlow()

    // 2. Controla si se debe mostrar el diálogo de CONFIRMACIÓN de borrado
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog = _showDeleteDialog.asStateFlow()

    // 3. Guarda la acción (Importar o Actualizar) antes de abrir el picker
    private val _currentFileAction = MutableStateFlow(FileAction.IMPORT)
    // --- FIN MODIFICADO ---

    /**
     * Llamado con la pulsación larga. Muestra el diálogo de OPCIONES.
     */
    fun onSubjectLongPress(subject: SubjectEntity) {
        _selectedSubject.value = subject
    }

    /**
     * Cierra el diálogo de OPCIONES.
     */
    fun onDismissOptionsDialog() {
        _selectedSubject.value = null
    }

    /**
     * Llamado desde el diálogo de Opciones. Prepara el diálogo de CONFIRMACIÓN de borrado.
     */
    fun onDeleteClicked() {
        _showDeleteDialog.value = true
        // Mantenemos _selectedSubject con valor para saber qué borrar
    }

    /**
     * Cierra el diálogo de CONFIRMACIÓN de borrado.
     */
    fun onDismissDeleteDialog() {
        _showDeleteDialog.value = false
        _selectedSubject.value = null // Limpiamos la selección
    }

    /**
     * Confirma el borrado final.
     */
    fun onConfirmDelete() {
        _selectedSubject.value?.let { subject ->
            viewModelScope.launch {
                try {
                    repository.deleteSubject(subject.id)
                    Toast.makeText(application, "${subject.name} eliminado", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(application, "Error al eliminar la materia", Toast.LENGTH_LONG).show()
                } finally {
                    onDismissDeleteDialog() // Cierra el diálogo y limpia
                }
            }
        }
    }

    /**
     * Prepara al ViewModel para la acción de archivo (Importar o Actualizar).
     * Se llama JUSTO ANTES de lanzar el file picker.
     */
    fun prepareForFileAction(action: FileAction) {
        _currentFileAction.value = action
        // Si la acción es IMPORTAR, nos aseguramos de que no haya ninguna materia seleccionada
        if (action == FileAction.IMPORT) {
            _selectedSubject.value = null
        }
    }

    /**
     * Función central que recibe el URI del file picker y decide qué hacer.
     */
    fun processFile(uri: Uri) {
        when (_currentFileAction.value) {
            FileAction.IMPORT -> importMateria(uri)
            FileAction.UPDATE -> updateMateria(uri)
        }
    }

    /**
     * Es llamado desde la UI cuando el usuario selecciona un archivo JSON (Importar).
     * Ahora es PRIVADA.
     */
    private fun importMateria(uri: Uri) {
        viewModelScope.launch {
            try {
                // 1. Leer el contenido del archivo JSON
                val jsonString = readTextFromUri(uri)
                if (jsonString == null) {
                    Toast.makeText(application, "Error: No se pudo leer el archivo", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // 2. Llamar al repositorio para que haga la importación
                repository.importSubjectFromJson(jsonString)

                // 3. Notificar al usuario (¡la lista se actualizará sola gracias a Flow!)
                Toast.makeText(application, "¡Materia importada con éxito!", Toast.LENGTH_SHORT).show()

            } catch (e: SerializationException) {
                // Error si el JSON está mal formateado
                e.printStackTrace()
                Toast.makeText(application, "Error: El formato del JSON es incorrecto.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                // Cualquier otro error
                e.printStackTrace()
                Log.e("SubjectViewModel", "Error al importar", e)
                Toast.makeText(application, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- AÑADIDA: Nueva función para ACTUALIZAR ---
    /**
     * Llama al repositorio para ACTUALIZAR una materia existente.
     * Es PRIVADA.
     */
    private fun updateMateria(uri: Uri) {
        val subjectToUpdate = _selectedSubject.value
        if (subjectToUpdate == null) {
            Toast.makeText(application, "Error: No se seleccionó ninguna materia para actualizar", Toast.LENGTH_LONG).show()
            return
        }

        viewModelScope.launch {
            try {
                // 1. Leer el contenido del archivo JSON
                val jsonString = readTextFromUri(uri)
                if (jsonString == null) {
                    Toast.makeText(application, "Error: No se pudo leer el archivo", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // 2. Llamar al repositorio para que haga la ACTUALIZACIÓN
                repository.updateSubjectFromJson(subjectToUpdate.id, jsonString)

                // 3. Notificar al usuario
                Toast.makeText(application, "¡Materia actualizada con éxito!", Toast.LENGTH_SHORT).show()

            } catch (e: SerializationException) {
                e.printStackTrace()
                Toast.makeText(application, "Error: El formato del JSON es incorrecto.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SubjectViewModel", "Error al actualizar", e)
                Toast.makeText(application, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                // Limpiamos la selección después de la operación
                _selectedSubject.value = null
            }
        }
    }
    // --- FIN FUNCIÓN AÑADIDA ---


    /**
     * Función ayudante para leer un URI y devolver su contenido como String.
     */
    private fun readTextFromUri(uri: Uri): String? {
        return try {
            application.contentResolver.openInputStream(uri)?.bufferedReader().use {
                it?.readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}