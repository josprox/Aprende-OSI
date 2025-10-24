package com.josprox.redesosi.vm


// ... (otros imports)
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josprox.redesosi.data.database.ModuleEntity
import com.josprox.redesosi.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

// ViewModel anidado para simplificar la inyección con subjectId
@HiltViewModel
class ModuleListViewModel @Inject constructor(
    private val repository: StudyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val subjectId: Int = checkNotNull(savedStateHandle["subjectId"])
    fun getModules(): Flow<List<ModuleEntity>> = repository.getModulesForSubject(subjectId)

    private val _subjectName = MutableStateFlow("Módulos")
    val subjectName = _subjectName.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllSubjects().firstOrNull()?.find { it.id == subjectId }?.let { subject ->
                _subjectName.value = subject.name
            }
        }
    }
}