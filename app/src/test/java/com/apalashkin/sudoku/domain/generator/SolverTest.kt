package com.apalashkin.sudoku.domain.generator

import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Coord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SolverTest {

    @Test
    fun `completed valid board yields unique with itself`() {
        val grid = arrayOf(
            intArrayOf(5, 3, 4, 6, 7, 8, 9, 1, 2),
            intArrayOf(6, 7, 2, 1, 9, 5, 3, 4, 8),
            intArrayOf(1, 9, 8, 3, 4, 2, 5, 6, 7),
            intArrayOf(8, 5, 9, 7, 6, 1, 4, 2, 3),
            intArrayOf(4, 2, 6, 8, 5, 3, 7, 9, 1),
            intArrayOf(7, 1, 3, 9, 2, 4, 8, 5, 6),
            intArrayOf(9, 6, 1, 5, 3, 7, 2, 8, 4),
            intArrayOf(2, 8, 7, 4, 1, 9, 6, 3, 5),
            intArrayOf(3, 4, 5, 2, 8, 6, 1, 7, 9),
        )
        val board = Board.fromGrid(grid)
        val result = Solver.solve(board)

        assertTrue(result is SolveResult.Unique)
        assertBoardsEqual(board, (result as SolveResult.Unique).solution)
    }

    @Test
    fun `empty board has multiple solutions`() {
        assertEquals(SolveResult.Multiple, Solver.solve(Board.empty()))
    }

    @Test
    fun `board with duplicate in row is unsolvable`() {
        val grid = Array(9) { IntArray(9) }
        grid[0][0] = 5
        grid[0][1] = 5
        assertEquals(SolveResult.None, Solver.solve(Board.fromGrid(grid)))
    }

    @Test
    fun `board with duplicate in column is unsolvable`() {
        val grid = Array(9) { IntArray(9) }
        grid[0][0] = 5
        grid[1][0] = 5
        assertEquals(SolveResult.None, Solver.solve(Board.fromGrid(grid)))
    }

    @Test
    fun `board with duplicate in box is unsolvable`() {
        val grid = Array(9) { IntArray(9) }
        grid[0][0] = 5
        grid[1][1] = 5
        assertEquals(SolveResult.None, Solver.solve(Board.fromGrid(grid)))
    }

    @Test
    fun `hard puzzle with unique solution is solved`() {
        val grid = arrayOf(
            intArrayOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
            intArrayOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
            intArrayOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
            intArrayOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
            intArrayOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
            intArrayOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
            intArrayOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
            intArrayOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
            intArrayOf(0, 0, 0, 0, 8, 0, 0, 7, 9),
        )
        val board = Board.fromGrid(grid)
        val result = Solver.solve(board)

        assertTrue("expected Unique, was $result", result is SolveResult.Unique)
        val solved = (result as SolveResult.Unique).solution
        for (r in 0..8) for (c in 0..8) {
            assertTrue("cell ($r,$c) not filled", solved.cell(Coord(r, c)).value != null)
        }
    }

    @Test
    fun `puzzle with two missing cells in same row has unique solution when only one digit fits`() {
        val grid = arrayOf(
            intArrayOf(5, 3, 4, 6, 7, 8, 9, 1, 2),
            intArrayOf(6, 7, 2, 1, 9, 5, 3, 4, 8),
            intArrayOf(1, 9, 8, 3, 4, 2, 5, 6, 7),
            intArrayOf(8, 5, 9, 7, 6, 1, 4, 2, 3),
            intArrayOf(4, 2, 6, 8, 5, 3, 7, 9, 1),
            intArrayOf(7, 1, 3, 9, 2, 4, 8, 5, 6),
            intArrayOf(9, 6, 1, 5, 3, 7, 2, 8, 4),
            intArrayOf(2, 8, 7, 4, 1, 9, 6, 3, 5),
            intArrayOf(3, 4, 5, 2, 8, 6, 1, 0, 0),
        )
        val result = Solver.solve(Board.fromGrid(grid))
        assertTrue(result is SolveResult.Unique)
        val solved = (result as SolveResult.Unique).solution
        assertEquals(7, solved.cell(Coord(8, 7)).value)
        assertEquals(9, solved.cell(Coord(8, 8)).value)
    }

    @Test
    fun `puzzle with many missing cells and ambiguous fill has multiple solutions`() {
        val grid = Array(9) { IntArray(9) }
        grid[0][0] = 1
        assertEquals(SolveResult.Multiple, Solver.solve(Board.fromGrid(grid)))
    }

    private fun assertBoardsEqual(expected: Board, actual: Board) {
        for (r in 0..8) for (c in 0..8) {
            val coord = Coord(r, c)
            assertEquals("mismatch at ($r,$c)", expected.cell(coord).value, actual.cell(coord).value)
        }
    }
}
