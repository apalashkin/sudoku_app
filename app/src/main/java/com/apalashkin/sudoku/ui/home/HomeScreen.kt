package com.apalashkin.sudoku.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apalashkin.sudoku.domain.generator.Difficulty

@Composable
fun HomeScreen(
    onNavigateToGame: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val active by viewModel.activeGame.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Sudoku",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (active != null) {
            Button(
                onClick = onNavigateToGame,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue (${active!!.state.difficulty.name})")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "or start a new game",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Difficulty.entries.forEach { difficulty ->
            FilledTonalButton(
                onClick = { viewModel.startNewGame(difficulty, onNavigateToGame) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("New game · ${difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onNavigateToStats,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Stats")
        }
        OutlinedButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Settings")
        }
    }
}
