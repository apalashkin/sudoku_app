package com.apalashkin.sudoku.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val peers = remember(state.selected) { state.selected?.peers().orEmpty() }
    val mistakes = remember(state.puzzle) { state.mistakes() }
    val sameDigit = remember(state.selected, state.puzzle) { state.sameDigitCells() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Toolbar(
            canUndo = state.canUndo,
            noteMode = state.noteMode,
            onUndo = viewModel::undo,
            onToggleNoteMode = viewModel::toggleNoteMode,
        )
        if (state.isComplete) {
            Text(
                text = "🎉 Solved!",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        GridView(
            board = state.puzzle,
            selected = state.selected,
            peers = peers,
            sameDigit = sameDigit,
            mistakes = mistakes,
            onCellTap = viewModel::selectCell,
        )
        NumberPad(
            noteMode = state.noteMode,
            onNumberTap = viewModel::placeDigit,
            onErase = viewModel::erase,
        )
    }
}

@Composable
private fun Toolbar(
    canUndo: Boolean,
    noteMode: Boolean,
    onUndo: () -> Unit,
    onToggleNoteMode: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    ) {
        IconButton(onClick = onUndo, enabled = canUndo) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
            )
        }
        if (noteMode) {
            FilledIconButton(onClick = onToggleNoteMode) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Notes mode (on)")
            }
        } else {
            IconButton(
                onClick = onToggleNoteMode,
                colors = IconButtonDefaults.iconButtonColors(),
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Notes mode (off)")
            }
        }
    }
}
