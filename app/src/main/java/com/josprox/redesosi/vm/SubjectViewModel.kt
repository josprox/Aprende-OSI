package com.josprox.redesosi.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josprox.redesosi.data.database.SubjectEntity
import com.josprox.redesosi.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(
    studyRepository: StudyRepository
) : ViewModel() {
    val subjects: StateFlow<List<SubjectEntity>> = studyRepository.getAllSubjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
