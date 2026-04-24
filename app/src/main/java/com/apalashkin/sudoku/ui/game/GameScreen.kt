package com.apalashkin.sudoku.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Coord

private val DEMO_PUZZLE: Array<IntArray> = arrayOf(
    intArrayOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
    intArrayOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
    intArrayOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
    intArrayOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
    intArrayOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
    intArrayOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
    intArrayOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
    intArrayOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
    intArrayOf(0, 0, 0, 0, 8, 0, 0, 7, 9),
)

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    val board = remember { Board.fromGrid(DEMO_PUZZLE) }
    var selected by remember { mutableStateOf<Coord?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GridView(
            board = board,
            selected = selected,
            onCellTap = { coord -> selected = coord },
        )
        NumberPad(onNumberTap = { /* Phase 3 */ }, onErase = { /* Phase 3 */ })
    }
}
