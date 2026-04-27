package com.apalashkin.sudoku.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Cell
import com.apalashkin.sudoku.domain.model.Coord

@Composable
fun GridView(
    board: Board,
    selected: Coord?,
    peers: Set<Coord>,
    sameDigit: Set<Coord>,
    mistakes: Set<Coord>,
    onCellTap: (Coord) -> Unit,
    modifier: Modifier = Modifier,
) {
    val thinColor = MaterialTheme.colorScheme.outlineVariant
    val thickColor = MaterialTheme.colorScheme.onSurface
    val selectedBg = MaterialTheme.colorScheme.primaryContainer
    val sameDigitBg = MaterialTheme.colorScheme.secondaryContainer
    val peerBg = MaterialTheme.colorScheme.surfaceVariant
    val mistakeColor = MaterialTheme.colorScheme.error
    val givenColor = MaterialTheme.colorScheme.onSurface
    val userColor = MaterialTheme.colorScheme.primary
    val noteColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .border(width = 2.dp, color = thickColor),
    ) {
        for (row in 0..8) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                for (col in 0..8) {
                    val coord = Coord(row, col)
                    val cell = board.cell(coord)
                    val isSelected = selected == coord
                    val isMistake = coord in mistakes
                    val isSameDigit = !isSelected && coord in sameDigit
                    val isPeer = !isSelected && !isSameDigit && coord in peers

                    val bg = when {
                        isSelected -> selectedBg
                        isSameDigit -> sameDigitBg
                        isPeer -> peerBg
                        else -> Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .background(bg)
                            .clickable { onCellTap(coord) }
                            .drawBehind {
                                val thin = 0.5.dp.toPx()
                                val thick = 2.dp.toPx()
                                val topWidth = when {
                                    row == 0 -> 0f
                                    row % 3 == 0 -> thick
                                    else -> thin
                                }
                                val leftWidth = when {
                                    col == 0 -> 0f
                                    col % 3 == 0 -> thick
                                    else -> thin
                                }
                                if (topWidth > 0f) {
                                    val y = topWidth / 2f
                                    drawLine(
                                        color = if (row % 3 == 0) thickColor else thinColor,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = topWidth,
                                    )
                                }
                                if (leftWidth > 0f) {
                                    val x = leftWidth / 2f
                                    drawLine(
                                        color = if (col % 3 == 0) thickColor else thinColor,
                                        start = Offset(x, 0f),
                                        end = Offset(x, size.height),
                                        strokeWidth = leftWidth,
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CellContent(
                                cell = cell,
                                isMistake = isMistake,
                                mistakeColor = mistakeColor,
                                givenColor = givenColor,
                                userColor = userColor,
                                noteColor = noteColor,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CellContent(
    cell: Cell,
    isMistake: Boolean,
    mistakeColor: Color,
    givenColor: Color,
    userColor: Color,
    noteColor: Color,
) {
    val value = cell.value
    if (value != null) {
        Text(
            text = value.toString(),
            fontSize = 20.sp,
            fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isMistake -> mistakeColor
                cell.isGiven -> givenColor
                else -> userColor
            },
        )
    } else if (cell.notes.isNotEmpty()) {
        NotesGrid(notes = cell.notes, color = noteColor)
    }
}

@Composable
private fun NotesGrid(notes: Set<Int>, color: Color) {
    Column(modifier = Modifier.fillMaxSize()) {
        for (r in 0..2) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                for (c in 0..2) {
                    val n = r * 3 + c + 1
                    Box(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (n in notes) {
                            Text(
                                text = n.toString(),
                                fontSize = 11.sp,
                                lineHeight = 11.sp,
                                color = color,
                                fontFamily = FontFamily.SansSerif,
                                style = LocalTextStyle.current.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}
