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
import androidx.compose.ui.graphics.luminance
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
    activeDigit: Int?,
    mistakes: Set<Coord>,
    onCellTap: (Coord) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val thinColor = MaterialTheme.colorScheme.outlineVariant
    val thickColor = MaterialTheme.colorScheme.onSurface
    val selectedBg = if (isDark) Color(0xFF595959) else Color(0xFFEEEEEE)
    val peerBg = selectedBg
    val sameDigitBg = MaterialTheme.colorScheme.primary
    val sameDigitFg = MaterialTheme.colorScheme.onPrimary
    val mistakeBg = if (isDark) Color(0xFF5D1010) else Color(0xFFFFCDD2)
    val mistakeColor = if (isDark) Color(0xFFFFCDD2) else Color(0xFFB71C1C)
    val givenColor = if (isDark) Color(0xFFEEEEEE) else Color(0xFF212121)
    val userColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF1565C0)
    val noteColor = MaterialTheme.colorScheme.onSurfaceVariant
    val noteHighlightBg = sameDigitBg
    val noteHighlightFg = sameDigitFg

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
                    val isSameDigit = activeDigit != null && cell.value == activeDigit
                    val isPeer = !isSameDigit && coord in peers

                    val bg = when {
                        isMistake -> mistakeBg
                        isSameDigit -> sameDigitBg
                        isSelected -> selectedBg
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
                                isSameDigit = isSameDigit,
                                activeDigit = activeDigit,
                                mistakeColor = mistakeColor,
                                sameDigitFg = sameDigitFg,
                                givenColor = givenColor,
                                userColor = userColor,
                                noteColor = noteColor,
                                noteHighlightBg = noteHighlightBg,
                                noteHighlightFg = noteHighlightFg,
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
    isSameDigit: Boolean,
    activeDigit: Int?,
    mistakeColor: Color,
    sameDigitFg: Color,
    givenColor: Color,
    userColor: Color,
    noteColor: Color,
    noteHighlightBg: Color,
    noteHighlightFg: Color,
) {
    val value = cell.value
    if (value != null) {
        Text(
            text = value.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                isMistake -> mistakeColor
                isSameDigit -> sameDigitFg
                cell.isGiven -> givenColor
                else -> userColor
            },
        )
    } else if (cell.notes.isNotEmpty()) {
        NotesGrid(
            notes = cell.notes,
            color = noteColor,
            activeDigit = activeDigit,
            highlightBg = noteHighlightBg,
            highlightFg = noteHighlightFg,
        )
    }
}

@Composable
private fun NotesGrid(
    notes: Set<Int>,
    color: Color,
    activeDigit: Int?,
    highlightBg: Color,
    highlightFg: Color,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        for (r in 0..2) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                for (c in 0..2) {
                    val n = r * 3 + c + 1
                    val isHighlighted = activeDigit == n && n in notes
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .background(if (isHighlighted) highlightBg else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (n in notes) {
                            Text(
                                text = n.toString(),
                                fontSize = 11.sp,
                                lineHeight = 11.sp,
                                color = if (isHighlighted) highlightFg else color,
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
