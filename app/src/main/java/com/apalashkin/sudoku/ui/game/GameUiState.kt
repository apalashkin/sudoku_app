package com.apalashkin.sudoku.ui.game

import com.apalashkin.sudoku.domain.generator.Puzzle
import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Cell
import com.apalashkin.sudoku.domain.model.Coord

data class GameUiState(
    val puzzle: Board,
    val solution: Board,
    val selected: Coord? = null,
    val isComplete: Boolean = false,
) {

    fun selectCell(coord: Coord): GameUiState = copy(selected = coord)

    fun placeDigit(digit: Int): GameUiState {
        require(digit in 1..9) { "digit must be 1..9, was $digit" }
        if (isComplete) return this
        val coord = selected ?: return this
        if (puzzle.cell(coord).isGiven) return this

        val updatedPuzzle = puzzle.withCell(coord, Cell.filled(digit))
        return copy(
            puzzle = updatedPuzzle,
            isComplete = matchesSolution(updatedPuzzle),
        )
    }

    fun erase(): GameUiState {
        if (isComplete) return this
        val coord = selected ?: return this
        if (puzzle.cell(coord).isGiven) return this
        return copy(puzzle = puzzle.withCell(coord, Cell.empty()))
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
        )
    }
}
