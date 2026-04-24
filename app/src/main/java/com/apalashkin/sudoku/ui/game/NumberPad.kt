package com.apalashkin.sudoku.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumberPad(
    onNumberTap: (Int) -> Unit,
    onErase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (n in 1..9) {
            OutlinedButton(
                onClick = { onNumberTap(n) },
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            ) {
                Text(text = n.toString())
            }
        }
        OutlinedButton(
            onClick = onErase,
            modifier = Modifier.weight(1.2f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        ) {
            Text(text = "⌫")
        }
    }
}
