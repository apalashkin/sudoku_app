package com.apalashkin.sudoku.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun GameScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                delay(1_000)
                viewModel.tick(1_000)
            }
        }
    }

    val current = state
    if (current == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val peers = remember(current.selected) { current.selected?.peers().orEmpty() }
    val mistakes = remember(current.puzzle) { current.mistakes() }
    val completed = remember(current.puzzle) { current.completedDigits() }
    val remaining = remember(current.puzzle) { current.digitsRemaining() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Toolbar(
            canUndo = current.canUndo,
            noteMode = current.noteMode,
            pencilMode = current.pencilMode,
            elapsedMs = current.elapsedMs,
            mistakeCount = current.mistakeCount,
            maxMistakes = current.maxMistakes,
            onBack = onNavigateBack,
            onUndo = viewModel::undo,
            onErase = viewModel::erase,
            onToggleNoteMode = viewModel::toggleNoteMode,
            onTogglePencilMode = viewModel::togglePencilMode,
        )
        when {
            current.isComplete -> Text(
                text = "🎉 Solved!",
                style = MaterialTheme.typography.titleLarge,
            )
            current.isFailed -> Text(
                text = "❌ Out of mistakes",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
        GridView(
            board = current.puzzle,
            selected = current.selected,
            peers = peers,
            activeDigit = current.activeDigit,
            mistakes = mistakes,
            onCellTap = viewModel::selectCell,
        )
        NumberPad(
            noteMode = current.noteMode,
            selectedDigit = current.selectedDigit,
            completedDigits = completed,
            digitsRemaining = remaining,
            onNumberTap = viewModel::selectDigit,
        )
    }
}

@Composable
private fun Toolbar(
    canUndo: Boolean,
    noteMode: Boolean,
    pencilMode: Boolean,
    elapsedMs: Long,
    mistakeCount: Int,
    maxMistakes: Int,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onErase: () -> Unit,
    onToggleNoteMode: () -> Unit,
    onTogglePencilMode: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
            )
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text(
                text = formatElapsed(elapsedMs),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = formatMistakes(mistakeCount, maxMistakes),
                style = MaterialTheme.typography.bodySmall,
                color = if (mistakeCount > 0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(onClick = onUndo, enabled = canUndo) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                )
            }
            IconButton(onClick = onErase) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Erase",
                )
            }
            if (pencilMode) {
                FilledIconButton(onClick = onTogglePencilMode) {
                    Icon(imageVector = Icons.Default.PushPin, contentDescription = "Lock digit (on)")
                }
            } else {
                IconButton(onClick = onTogglePencilMode) {
                    Icon(imageVector = Icons.Default.PushPin, contentDescription = "Lock digit (off)")
                }
            }
            if (noteMode) {
                FilledIconButton(onClick = onToggleNoteMode) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Notes mode (on)")
                }
            } else {
                IconButton(onClick = onToggleNoteMode) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Notes mode (off)")
                }
            }
        }
    }
}

private fun formatElapsed(ms: Long): String {
    val totalSeconds = ms / 1000
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return "%02d:%02d".format(mins, secs)
}

private fun formatMistakes(count: Int, max: Int): String =
    if (max == Int.MAX_VALUE) "mistakes $count" else "mistakes $count/$max"
