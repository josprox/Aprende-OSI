package com.josprox.redesosi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.josprox.redesosi.navigation.AppNavGraph
import com.josprox.redesosi.ui.theme.RedesOSITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedesOSITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. Se crea el NavController aquí, en el nivel más alto.
                    val navController = rememberNavController()
                    // 2. Se pasa el NavController al grafo de navegación.
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}

