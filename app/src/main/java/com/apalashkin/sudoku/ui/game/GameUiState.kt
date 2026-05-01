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
    val noteMode: Boolean = false,
    val isComplete: Boolean = false,
    val history: List<Board> = emptyList(),
) {

    val canUndo: Boolean get() = history.isNotEmpty()

    fun selectCell(coord: Coord): GameUiState = copy(selected = coord)

    fun toggleNoteMode(): GameUiState = copy(noteMode = !noteMode)

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

    fun sameDigitCells(): Set<Coord> {
        val coord = selected ?: return emptySet()
        val v = puzzle.cell(coord).value ?: return emptySet()
        val result = mutableSetOf<Coord>()
        for (r in 0..8) for (c in 0..8) {
            val c2 = Coord(r, c)
            if (puzzle.cell(c2).value == v) result += c2
        }
        return result
    }

    fun undo(): GameUiState {
        val previous = history.lastOrNull() ?: return this
        return copy(
            puzzle = previous,
            history = history.dropLast(1),
            isComplete = matchesSolution(previous),
        )
    }

    private fun commitMutation(updatedPuzzle: Board): GameUiState {
        if (updatedPuzzle == puzzle) return this
        return copy(
            puzzle = updatedPuzzle,
            history = history + puzzle,
            isComplete = matchesSolution(updatedPuzzle),
        )
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
