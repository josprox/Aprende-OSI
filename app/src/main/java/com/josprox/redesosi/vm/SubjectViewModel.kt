package com.josprox.redesosi.vm

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josprox.redesosi.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import com.josprox.redesosi.data.database.SubjectEntity // <-- AÑADIR IMPORT
import kotlinx.coroutines.flow.MutableStateFlow // <-- AÑADIR IMPORT
import kotlinx.coroutines.flow.asStateFlow // <-- AÑADIR IMPORT

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val repository: StudyRepository,
    private val application: Application // Hilt inyectará el contexto
) : ViewModel() {

    val subjects = repository.getAllSubjects()

    // --- AÑADIDO: Lógica para el diálogo de eliminación ---
    private val _subjectToDelete = MutableStateFlow<SubjectEntity?>(null)
    val subjectToDelete = _subjectToDelete.asStateFlow()

    fun onSubjectLongPress(subject: SubjectEntity) {
        _subjectToDelete.value = subject
    }

    fun onDismissDeleteDialog() {
        _subjectToDelete.value = null
    }

    fun onConfirmDelete() {
        _subjectToDelete.value?.let { subject ->
            viewModelScope.launch {
                try {
                    repository.deleteSubject(subject.id)
                    Toast.makeText(application, "${subject.name} eliminado", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(application, "Error al eliminar la materia", Toast.LENGTH_LONG).show()
                } finally {
                    _subjectToDelete.value = null // Cierra el diálogo
                }
            }
        }
    }

    /**
     * Es llamado desde la UI cuando el usuario selecciona un archivo JSON.
     */
    fun importMateria(uri: Uri) {
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
                Toast.makeText(application, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

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