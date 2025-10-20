package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.josprox.redesosi.data.database.ModuleEntity
import com.josprox.redesosi.data.repository.StudyRepository
import com.josprox.redesosi.navigation.AppScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// ViewModel anidado para simplificar la inyección con subjectId
@HiltViewModel
class ModuleListViewModel @Inject constructor(
    private val repository: StudyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val subjectId: Int = checkNotNull(savedStateHandle["subjectId"])
    fun getModules(): Flow<List<ModuleEntity>> = repository.getModulesForSubject(subjectId)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleListScreen(
    navController: NavController,
    subjectId: Int,
    viewModel: ModuleListViewModel = hiltViewModel()
) {
    val modules by viewModel.getModules().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Módulos") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(modules) { module ->
                Card(
                    onClick = { navController.navigate(AppScreen.ModuleDetail.createRoute(module.id)) }
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(text = module.title, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = module.shortDescription, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
