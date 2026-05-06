package com.apalashkin.sudoku.ui.game

import com.apalashkin.sudoku.domain.generator.Difficulty
import com.apalashkin.sudoku.domain.generator.Puzzle
import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Cell
import com.apalashkin.sudoku.domain.model.Coord

data class GameUiState(
    val puzzle: Board,
    val solution: Board,
    val difficulty: Difficulty = Difficulty.EASY,
    val selected: Coord? = null,
    val selectedDigit: Int? = null,
    val noteMode: Boolean = false,
    val pencilMode: Boolean = false,
    val isComplete: Boolean = false,
    val history: List<Board> = emptyList(),
    val elapsedMs: Long = 0L,
) {

    val canUndo: Boolean get() = history.isNotEmpty()

    val activeDigit: Int? get() = selectedDigit ?: selected?.let { puzzle.cell(it).value }

    fun tick(deltaMs: Long): GameUiState {
        if (isComplete) return this
        return copy(elapsedMs = elapsedMs + deltaMs)
    }

    fun selectCell(coord: Coord): GameUiState {
        if (!pencilMode) return copy(selected = coord)
        val cell = puzzle.cell(coord)
        if (cell.value != null) return copy(selected = coord, selectedDigit = cell.value)
        val withCell = copy(selected = coord)
        return if (selectedDigit != null) withCell.placeDigit(selectedDigit) else withCell
    }

    fun selectDigit(digit: Int): GameUiState {
        require(digit in 1..9) { "digit must be 1..9, was $digit" }
        if (!pencilMode) return placeDigit(digit)
        if (selectedDigit == digit) return this
        return copy(selectedDigit = digit)
    }

    fun toggleNoteMode(): GameUiState = copy(noteMode = !noteMode)

    fun togglePencilMode(): GameUiState =
        if (pencilMode) copy(pencilMode = false, selectedDigit = null)
        else copy(pencilMode = true)

    fun placeDigit(digit: Int): GameUiState {
        require(digit in 1..9) { "digit must be 1..9, was $digit" }
        if (isComplete) return this
        val coord = selected ?: return this
        val cell = puzzle.cell(coord)
        if (cell.isGiven) return this

        val updatedPuzzle = if (noteMode) {
            if (cell.value != null) return this
            val updatedNotes = if (digit in cell.notes) cell.notes - digit else cell.notes + digit
            puzzle.withCell(coord, cell.copy(notes = updatedNotes))
        } else {
            val placed = puzzle.withCell(coord, Cell.filled(digit))
            clearDigitFromPeerNotes(placed, coord, digit)
        }
        return commitMutation(updatedPuzzle)
    }

    fun erase(): GameUiState {
        if (isComplete) return this
        val coord = selected ?: return this
        val cell = puzzle.cell(coord)
        if (cell.isGiven) return this
        val updatedPuzzle = puzzle.withCell(coord, Cell.empty())
        return commitMutation(updatedPuzzle)
    }

    fun mistakes(): Set<Coord> {
        val result = mutableSetOf<Coord>()
        for (r in 0..8) for (c in 0..8) {
            val coord = Coord(r, c)
            val v = puzzle.cell(coord).value ?: continue
            for (peer in coord.peers()) {
                if (puzzle.cell(peer).value == v) {
                    result += coord
                    break
                }
            }
        }
        return result
    }

    fun digitHighlights(): Set<Coord> {
        val d = selectedDigit ?: selected?.let { puzzle.cell(it).value } ?: return emptySet()
        val result = mutableSetOf<Coord>()
        for (r in 0..8) for (c in 0..8) {
            val coord = Coord(r, c)
            val cell = puzzle.cell(coord)
            if (cell.value == d || d in cell.notes) result += coord
        }
        return result
    }

    fun completedDigits(): Set<Int> {
        val mistakeDigits = mistakes().mapNotNull { puzzle.cell(it).value }.toSet()
        val counts = digitCounts()
        return (1..9).filter { it !in mistakeDigits && counts[it] >= 9 }.toSet()
    }

    fun digitsRemaining(): Map<Int, Int> {
        val counts = digitCounts()
        return (1..9).associateWith { (9 - counts[it]).coerceAtLeast(0) }
    }

    fun undo(): GameUiState {
        val previous = history.lastOrNull() ?: return this
        return copy(
            puzzle = previous,
            history = history.dropLast(1),
            isComplete = matchesSolution(previous),
        )
    }

    private fun digitCounts(): IntArray {
        val counts = IntArray(10)
        for (r in 0..8) for (c in 0..8) {
            puzzle.cell(Coord(r, c)).value?.let { counts[it]++ }
        }
        return counts
    }

    private fun commitMutation(updatedPuzzle: Board): GameUiState {
        if (updatedPuzzle == puzzle) return this
        val intermediate = copy(
            puzzle = updatedPuzzle,
            history = history + puzzle,
            isComplete = matchesSolution(updatedPuzzle),
        )
        val locked = intermediate.selectedDigit ?: return intermediate
        val completed = intermediate.completedDigits()
        if (locked !in completed) return intermediate

        if (!intermediate.pencilMode) return intermediate.copy(selectedDigit = null)
        val next = (((locked + 1)..9) + (1 until locked)).firstOrNull { it !in completed }
        return intermediate.copy(selectedDigit = next)
    }

    private fun clearDigitFromPeerNotes(board: Board, coord: Coord, digit: Int): Board {
        var b = board
        for (peer in coord.peers()) {
            val cell = b.cell(peer)
            if (digit in cell.notes) {
                b = b.withCell(peer, cell.copy(notes = cell.notes - digit))
            }
        }
        return b
    }

    private fun matchesSolution(board: Board): Boolean {
        for (r in 0..8) for (c in 0..8) {
            val coord = Coord(r, c)
            if (board.cell(coord).value != solution.cell(coord).value) return false
        }
        return true
    }

    companion object {
        fun fromPuzzle(puzzle: Puzzle): GameUiState = GameUiState(
            puzzle = puzzle.puzzle,
            solution = puzzle.solution,
            difficulty = puzzle.difficulty,
        )
    }
}
