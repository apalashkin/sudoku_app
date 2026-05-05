package com.apalashkin.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apalashkin.sudoku.data.repository.ThemeMode
import com.apalashkin.sudoku.ui.game.GameScreen
import com.apalashkin.sudoku.ui.home.HomeScreen
import com.apalashkin.sudoku.ui.settings.SettingsScreen
import com.apalashkin.sudoku.ui.settings.SettingsViewModel
import com.apalashkin.sudoku.ui.stats.StatsScreen
import com.apalashkin.sudoku.ui.theme.SudokuAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsVm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            val settings by settingsVm.settings.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            SudokuAppTheme(darkTheme = darkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val nav = rememberNavController()
                    NavHost(
                        navController = nav,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable("home") {
                            HomeScreen(
                                onNavigateToGame = { nav.navigate("game") },
                                onNavigateToStats = { nav.navigate("stats") },
                                onNavigateToSettings = { nav.navigate("settings") },
                            )
                        }
                        composable("game") {
                            GameScreen(
                                onNavigateBack = { nav.popBackStack() },
                            )
                        }
                        composable("stats") {
                            StatsScreen(
                                onNavigateBack = { nav.popBackStack() },
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { nav.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }
}
