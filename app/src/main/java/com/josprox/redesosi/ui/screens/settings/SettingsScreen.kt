package com.josprox.redesosi.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.josprox.redesosi.navigation.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ajustes") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(top = 8.dp)) {
            ListItem(
                headlineContent = { Text("Backup y Restauración") },
                supportingContent = { Text("Exporta o importa todos tus datos") },
                leadingContent = { Icon(Icons.Default.Save, contentDescription = null) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null) },
                modifier = Modifier.clickable {
                    // Navegamos usando el NavController principal
                    navController.navigate(AppScreen.BackupRestore.route)
                }
            )
            Divider()
        }
    }
}