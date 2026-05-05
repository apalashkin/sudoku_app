package com.apalashkin.sudoku.data.serialization

import com.apalashkin.sudoku.domain.generator.Difficulty
import com.apalashkin.sudoku.domain.generator.PuzzleGenerator
import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Cell
import com.apalashkin.sudoku.domain.model.Coord
import com.apalashkin.sudoku.ui.game.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class GameStateJsonCodecTest {

    @Test
    fun `default state round-trips`() {
        val puzzle = PuzzleGenerator(Random(42)).generate(Difficulty.MEDIUM)
        val state = GameUiState.fromPuzzle(puzzle)
        val decoded = GameStateJsonCodec.decode(GameStateJsonCodec.encode(state))
        assertEquals(state, decoded)
    }

    @Test
    fun `state with selection and noteMode round-trips`() {
        val puzzle = PuzzleGenerator(Random(7)).generate(Difficulty.HARD)
        val state = GameUiState.fromPuzzle(puzzle)
            .selectCell(Coord(2, 3))
            .toggleNoteMode()
        val decoded = GameStateJsonCodec.decode(GameStateJsonCodec.encode(state))
        assertEquals(state, decoded)
        assertEquals(Coord(2, 3), decoded.selected)
        assertEquals(true, decoded.noteMode)
    }

    @Test
    fun `state with history round-trips`() {
        val puzzle = PuzzleGenerator(Random(99)).generate(Difficulty.EASY)
        val state = GameUiState.fromPuzzle(puzzle)
        var s = state
        repeat(3) {
            val empty = (0..8).flatMap { r -> (0..8).map { c -> Coord(r, c) } }
                .firstOrNull { s.puzzle.cell(it).isEmpty } ?: return@repeat
            s = s.selectCell(empty).placeDigit(1)
        }
        val decoded = GameStateJsonCodec.decode(GameStateJsonCodec.encode(s))
        assertEquals(s, decoded)
        assertEquals(s.history.size, decoded.history.size)
    }

    @Test
    fun `elapsedMs round-trips`() {
        val puzzle = PuzzleGenerator(Random(11)).generate(Difficulty.MEDIUM)
        val state = GameUiState.fromPuzzle(puzzle).tick(123_456L)
        val decoded = GameStateJsonCodec.decode(GameStateJsonCodec.encode(state))
        assertEquals(123_456L, decoded.elapsedMs)
    }

    @Test
    fun `state with notes round-trips`() {
        val board = Board.empty()
            .withCell(Coord(0, 0), Cell.empty().copy(notes = setOf(1, 4, 7)))
        val state = GameUiState(
            puzzle = board,
            solution = Board.empty(),
            difficulty = Difficulty.EXPERT,
        )
        val decoded = GameStateJsonCodec.decode(GameStateJsonCodec.encode(state))
        assertEquals(setOf(1, 4, 7), decoded.puzzle.cell(Coord(0, 0)).notes)
        assertEquals(Difficulty.EXPERT, decoded.difficulty)
    }
}
