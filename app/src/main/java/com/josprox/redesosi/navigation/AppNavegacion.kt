package com.josprox.redesosi.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.josprox.redesosi.ui.screens.PantallaAprende
import com.josprox.redesosi.ui.screens.PantallaCalificacion
import com.josprox.redesosi.ui.screens.PantallaTest
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppNavegacionPrincipal(
    navController: NavHostController
) {
    val pagerState = rememberPagerState { bottomNavigationItems.size }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavigationItems.forEachIndexed { index, screen ->
                    val isSelected = pagerState.currentPage == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        icon = { Icon(if (isSelected) screen.selectedIcon else screen.unselectedIcon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            key = { bottomNavigationItems[it].route }
        ) { pageIndex ->
            when (bottomNavigationItems[pageIndex]) {
                TabScreen.Aprende -> PantallaAprende(navController = navController)
                TabScreen.Test -> PantallaTest(navController = navController)
                TabScreen.Calificacion -> PantallaCalificacion(navController = navController)
            }
        }
    }
}