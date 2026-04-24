package com.apalashkin.sudoku.domain.generator

import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Coord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PuzzleGeneratorTest {

    @Test
    fun `generated solution is a fully filled valid board`() {
        val puzzle = PuzzleGenerator(Random(42)).generate(Difficulty.EASY)
        assertBoardFull(puzzle.solution)
    }

    @Test
    fun `generated puzzle has a unique solution`() {
        repeat(5) { seed ->
            val puzzle = PuzzleGenerator(Random(seed.toLong())).generate(Difficulty.MEDIUM)
            val result = Solver.solve(puzzle.puzzle)
            assertTrue("seed=$seed → $result", result is SolveResult.Unique)
        }
    }

    @Test
    fun `puzzle givens match the stored solution`() {
        val puzzle = PuzzleGenerator(Random(123)).generate(Difficulty.HARD)
        for (r in 0..8) for (c in 0..8) {
            val coord = Coord(r, c)
            val given = puzzle.puzzle.cell(coord).value
            if (given != null) {
                assertEquals("mismatch at ($r,$c)", given, puzzle.solution.cell(coord).value)
            }
        }
    }

    @Test
    fun `same seed produces the same puzzle`() {
        val a = PuzzleGenerator(Random(777)).generate(Difficulty.EASY)
        val b = PuzzleGenerator(Random(777)).generate(Difficulty.EASY)
        for (r in 0..8) for (c in 0..8) {
            val coord = Coord(r, c)
            assertEquals(a.puzzle.cell(coord).value, b.puzzle.cell(coord).value)
        }
    }

    @Test
    fun `different seeds produce different puzzles`() {
        val a = PuzzleGenerator(Random(1)).generate(Difficulty.EASY)
        val b = PuzzleGenerator(Random(2)).generate(Difficulty.EASY)
        assertNotEquals(boardValues(a.puzzle), boardValues(b.puzzle))
    }

    @Test
    fun `clue count is at or above the difficulty target`() {
        for (difficulty in Difficulty.entries) {
            repeat(3) { seed ->
                val puzzle = PuzzleGenerator(Random(seed.toLong())).generate(difficulty)
                val clues = countClues(puzzle.puzzle)
                assertTrue(
                    "difficulty=$difficulty seed=$seed clues=$clues target=${difficulty.targetClues}",
                    clues >= difficulty.targetClues,
                )
            }
        }
    }

    @Test
    fun `easy puzzle has more clues than expert`() {
        val easy = PuzzleGenerator(Random(50)).generate(Difficulty.EASY)
        val expert = PuzzleGenerator(Random(50)).generate(Difficulty.EXPERT)
        assertTrue(countClues(easy.puzzle) > countClues(expert.puzzle))
    }

    private fun assertBoardFull(board: Board) {
        for (r in 0..8) for (c in 0..8) {
            val v = board.cell(Coord(r, c)).value
            assertTrue("cell ($r,$c) empty", v != null)
            assertTrue(board.canPlace(Coord(r, c), v!!))
        }
    }

    private fun countClues(board: Board): Int {
        var n = 0
        for (r in 0..8) for (c in 0..8) {
            if (board.cell(Coord(r, c)).value != null) n++
        }
        return n
    }

    private fun boardValues(board: Board): List<Int?> =
        (0..8).flatMap { r -> (0..8).map { c -> board.cell(Coord(r, c)).value } }
}
