package com.josprox.redesosi.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josprox.redesosi.data.database.ModuleEntity
import com.josprox.redesosi.data.database.SubmoduleEntity
import com.josprox.redesosi.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ModuleViewModel @Inject constructor(
    private val repository: StudyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val subjectId: Int = checkNotNull(savedStateHandle["subjectId"])
    private val moduleId: Int = checkNotNull(savedStateHandle["moduleId"])

    val modules: StateFlow<List<ModuleEntity>> = repository.getModulesForSubject(subjectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val submodules: StateFlow<List<SubmoduleEntity>> = repository.getSubmodulesForModule(moduleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
