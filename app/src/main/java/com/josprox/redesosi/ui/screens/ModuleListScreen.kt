package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi // <-- AÑADIDO
import androidx.compose.foundation.basicMarquee // <-- AÑADIDO
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow // <-- IMPORTANTE: Lo quitamos
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.josprox.redesosi.data.database.ModuleEntity
import com.josprox.redesosi.data.repository.StudyRepository
import com.josprox.redesosi.navigation.AppScreen
import com.josprox.redesosi.vm.ModuleListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ModuleListScreen(
    navController: NavController,
    subjectId: Int,
    viewModel: ModuleListViewModel = hiltViewModel()
) {
    val modules by viewModel.getModules().collectAsState(initial = emptyList())
    val subjectName by viewModel.subjectName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // --- TÍTULO MODIFICADO CON MARQUEE ---
                    Text(
                        text = "$subjectName - Módulos",
                        // softWrap = false es crucial para que marquee funcione
                        softWrap = false,
                        modifier = Modifier.basicMarquee(
                            // iterations = Int.MAX_VALUE hace que se repita para siempre
                            iterations = Int.MAX_VALUE
                        )
                    )
                    // --- FIN DE LA MODIFICACIÓN ---
                },
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
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(modules) { module ->
                Card(
                    onClick = { navController.navigate(AppScreen.ModuleDetail.createRoute(module.id)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = module.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        supportingContent = {
                            Text(
                                text = module.shortDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Ver detalle",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}