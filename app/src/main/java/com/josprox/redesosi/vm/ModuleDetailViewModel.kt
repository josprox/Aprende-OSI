package com.josprox.redesosi.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josprox.redesosi.data.database.SubmoduleEntity
import com.josprox.redesosi.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//=================================================================
// 1. VIEWMODEL
//=================================================================
@HiltViewModel
class ModuleDetailViewModel @Inject constructor(
    private val repository: StudyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Obtenemos el ID del módulo desde el estado guardado
    val moduleId: Int = checkNotNull(savedStateHandle["moduleId"])

    // Este Flow de submódulos está perfecto
    val submodules: Flow<List<SubmoduleEntity>> = repository.getSubmodulesForModule(moduleId)

    // --- AÑADIDO: Lógica para obtener el título del módulo ---
    private val _moduleTitle = MutableStateFlow("Cargando...")
    val moduleTitle = _moduleTitle.asStateFlow()

    init {
        viewModelScope.launch {
            // Buscamos el módulo por su ID para obtener el título
            val module = repository.getModuleById(moduleId)
            _moduleTitle.value = module?.title ?: "Detalle del Módulo"
        }
    }
    // --- FIN AÑADIDO ---

    // --- Lógica del Diálogo (Para "Regenerar") ---
    private val _showConfirmDialog = MutableStateFlow(false)
    val showConfirmDialog = _showConfirmDialog.asStateFlow()

    // Esto se llama al presionar el botón de "Regenerar"
    fun onRegenerateClicked() {
        _showConfirmDialog.value = true
    }

    fun onDialogDismiss() {
        _showConfirmDialog.value = false
    }

    // Esto se llama al confirmar el diálogo
    fun onRegenerateConfirm() {
        _showConfirmDialog.value = false
        viewModelScope.launch {
            repository.forceRegenerateQuestions(moduleId)
        }
    }
}