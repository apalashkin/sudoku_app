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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Coord

@Composable
fun GridView(
    board: Board,
    selected: Coord?,
    onCellTap: (Coord) -> Unit,
    modifier: Modifier = Modifier,
) {
    val thinColor = MaterialTheme.colorScheme.outlineVariant
    val thickColor = MaterialTheme.colorScheme.onSurface

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

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
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
                                    drawLine(
                                        color = if (row % 3 == 0) thickColor else thinColor,
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, 0f),
                                        strokeWidth = topWidth,
                                    )
                                }
                                if (leftWidth > 0f) {
                                    drawLine(
                                        color = if (col % 3 == 0) thickColor else thinColor,
                                        start = Offset(0f, 0f),
                                        end = Offset(0f, size.height),
                                        strokeWidth = leftWidth,
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        cell.value?.let { v ->
                            Text(
                                text = v.toString(),
                                fontSize = 20.sp,
                                fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.Normal,
                                color = if (cell.isGiven) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}
