package com.apalashkin.sudoku.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumberPad(
    noteMode: Boolean,
    selectedDigit: Int?,
    completedDigits: Set<Int>,
    onNumberTap: (Int) -> Unit,
    onErase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (n in 1..9) {
            if (n in completedDigits) {
                Box(modifier = Modifier.weight(1f))
                continue
            }
            val isLocked = selectedDigit == n
            when {
                isLocked -> Button(
                    onClick = { onNumberTap(n) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                ) { Text(text = n.toString()) }
                noteMode -> FilledTonalButton(
                    onClick = { onNumberTap(n) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                ) { Text(text = n.toString()) }
                else -> OutlinedButton(
                    onClick = { onNumberTap(n) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                ) { Text(text = n.toString()) }
            }
        }
        OutlinedButton(
            onClick = onErase,
            modifier = Modifier.weight(1.2f),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(text = "⌫")
        }
    }
}
