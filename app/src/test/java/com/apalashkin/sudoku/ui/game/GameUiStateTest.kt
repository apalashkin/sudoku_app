package com.apalashkin.sudoku.ui.game

import com.apalashkin.sudoku.domain.generator.Difficulty
import com.apalashkin.sudoku.domain.generator.Puzzle
import com.apalashkin.sudoku.domain.generator.PuzzleGenerator
import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Cell
import com.apalashkin.sudoku.domain.model.Coord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameUiStateTest {

    private fun seededPuzzle(difficulty: Difficulty = Difficulty.EASY, seed: Long = 1L): Puzzle =
        PuzzleGenerator(Random(seed)).generate(difficulty)

    @Test
    fun `initial state has no selection and is not complete`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertNull(state.selected)
        assertFalse(state.isComplete)
    }

    @Test
    fun `selectCell updates the selection`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
            .selectCell(Coord(3, 4))
        assertEquals(Coord(3, 4), state.selected)
    }

    @Test
    fun `placeDigit with no selection is a no-op`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertEquals(state, state.placeDigit(5))
    }

    @Test
    fun `placeDigit on a given cell is a no-op`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val givenCoord = firstGivenCoord(state.puzzle)
        val after = state.selectCell(givenCoord).placeDigit(5)
        assertEquals(state.puzzle.cell(givenCoord).value, after.puzzle.cell(givenCoord).value)
    }

    @Test
    fun `placeDigit on an empty cell writes the value`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val emptyCoord = firstEmptyCoord(state.puzzle)
        val after = state.selectCell(emptyCoord).placeDigit(7)
        assertEquals(7, after.puzzle.cell(emptyCoord).value)
        assertFalse(after.puzzle.cell(emptyCoord).isGiven)
    }

    @Test
    fun `placeDigit replaces a user-filled value`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val emptyCoord = firstEmptyCoord(state.puzzle)
        val after = state
            .selectCell(emptyCoord)
            .placeDigit(3)
            .placeDigit(7)
        assertEquals(7, after.puzzle.cell(emptyCoord).value)
    }

    @Test
    fun `placeDigit rejects value outside 1 to 9`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
            .selectCell(firstEmptyCoord(
                GameUiState.fromPuzzle(seededPuzzle()).puzzle
            ))
        assertThrows(IllegalArgumentException::class.java) { state.placeDigit(0) }
        assertThrows(IllegalArgumentException::class.java) { state.placeDigit(10) }
    }

    @Test
    fun `erase with no selection is a no-op`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertEquals(state, state.erase())
    }

    @Test
    fun `erase on a given cell is a no-op`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val givenCoord = firstGivenCoord(state.puzzle)
        val after = state.selectCell(givenCoord).erase()
        assertEquals(state.puzzle.cell(givenCoord).value, after.puzzle.cell(givenCoord).value)
    }

    @Test
    fun `erase clears a user-filled value`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val emptyCoord = firstEmptyCoord(state.puzzle)
        val after = state
            .selectCell(emptyCoord)
            .placeDigit(7)
            .erase()
        assertNull(after.puzzle.cell(emptyCoord).value)
    }

    @Test
    fun `placing the last correct digit marks the game complete`() {
        val puzzle = seededPuzzle()
        val almostSolved = buildAlmostSolvedState(puzzle, leaveEmpty = 1)
        val lastCoord = firstEmptyCoord(almostSolved.puzzle)
        val correctValue = puzzle.solution.cell(lastCoord).value!!
        val after = almostSolved.selectCell(lastCoord).placeDigit(correctValue)
        assertTrue(after.isComplete)
    }

    @Test
    fun `placing a wrong digit does not mark the game complete`() {
        val puzzle = seededPuzzle()
        val almostSolved = buildAlmostSolvedState(puzzle, leaveEmpty = 1)
        val lastCoord = firstEmptyCoord(almostSolved.puzzle)
        val correctValue = puzzle.solution.cell(lastCoord).value!!
        val wrongValue = if (correctValue == 9) 1 else correctValue + 1
        val after = almostSolved.selectCell(lastCoord).placeDigit(wrongValue)
        assertFalse(after.isComplete)
    }

    @Test
    fun `placeDigit is a no-op once the game is complete`() {
        val puzzle = seededPuzzle()
        val state = buildAlmostSolvedState(puzzle, leaveEmpty = 1).let {
            val coord = firstEmptyCoord(it.puzzle)
            it.selectCell(coord).placeDigit(puzzle.solution.cell(coord).value!!)
        }
        assertTrue(state.isComplete)
        val coord = firstGivenCoord(state.puzzle)
        val after = state.selectCell(coord).placeDigit(1)
        assertEquals(state.puzzle.cell(coord).value, after.puzzle.cell(coord).value)
    }

    private fun firstGivenCoord(board: Board): Coord {
        for (r in 0..8) for (c in 0..8) {
            if (board.cell(Coord(r, c)).isGiven) return Coord(r, c)
        }
        error("no given cell")
    }

    private fun firstEmptyCoord(board: Board): Coord {
        for (r in 0..8) for (c in 0..8) {
            if (board.cell(Coord(r, c)).isEmpty) return Coord(r, c)
        }
        error("no empty cell")
    }

    private fun buildAlmostSolvedState(puzzle: Puzzle, leaveEmpty: Int): GameUiState {
        var board = puzzle.puzzle
        var left = leaveEmpty
        val emptyCoords = mutableListOf<Coord>()
        for (r in 0..8) for (c in 0..8) {
            val coord = Coord(r, c)
            if (board.cell(coord).isEmpty) emptyCoords += coord
        }
        val toFill = emptyCoords.dropLast(left)
        for (coord in toFill) {
            val v = puzzle.solution.cell(coord).value!!
            board = board.withCell(coord, Cell.filled(v))
        }
        return GameUiState(
            puzzle = board,
            solution = puzzle.solution,
            selected = null,
            isComplete = false,
        )
    }
}
