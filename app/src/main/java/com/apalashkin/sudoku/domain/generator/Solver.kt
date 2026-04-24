package com.apalashkin.sudoku.domain.generator

import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Coord

object Solver {

    fun solve(board: Board): SolveResult {
        for (r in 0..8) for (c in 0..8) {
            val v = board.cell(Coord(r, c)).value ?: continue
            if (!board.canPlace(Coord(r, c), v)) return SolveResult.None
        }
        val grid = boardToIntArray(board)
        val firstSolution = IntArray(81)
        val count = countSolutions(grid, limit = 2, firstSolution = firstSolution)
        return when (count) {
            0 -> SolveResult.None
            1 -> SolveResult.Unique(intArrayToBoard(firstSolution))
            else -> SolveResult.Multiple
        }
    }

    internal fun countSolutions(
        grid: IntArray,
        limit: Int,
        firstSolution: IntArray? = null,
    ): Int {
        val working = grid.copyOf()
        val count = intArrayOf(0)
        recurse(working, 0, firstSolution, count, limit)
        return count[0]
    }

    private fun recurse(
        grid: IntArray,
        idx: Int,
        firstSolution: IntArray?,
        count: IntArray,
        limit: Int,
    ) {
        if (count[0] >= limit) return
        if (idx == 81) {
            if (count[0] == 0 && firstSolution != null) grid.copyInto(firstSolution)
            count[0]++
            return
        }
        if (grid[idx] != 0) {
            recurse(grid, idx + 1, firstSolution, count, limit)
            return
        }
        val row = idx / 9
        val col = idx % 9
        for (v in 1..9) {
            if (isValid(grid, row, col, v)) {
                grid[idx] = v
                recurse(grid, idx + 1, firstSolution, count, limit)
                if (count[0] >= limit) {
                    grid[idx] = 0
                    return
                }
            }
        }
        grid[idx] = 0
    }

    internal fun isValid(grid: IntArray, row: Int, col: Int, value: Int): Boolean {
        for (i in 0..8) {
            if (grid[row * 9 + i] == value) return false
            if (grid[i * 9 + col] == value) return false
        }
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (grid[r * 9 + c] == value) return false
            }
        }
        return true
    }

    private fun boardToIntArray(board: Board): IntArray {
        val arr = IntArray(81)
        for (r in 0..8) for (c in 0..8) {
            arr[r * 9 + c] = board.cell(Coord(r, c)).value ?: 0
        }
        return arr
    }

    private fun intArrayToBoard(arr: IntArray): Board {
        val grid = Array(9) { r -> IntArray(9) { c -> arr[r * 9 + c] } }
        return Board.fromGrid(grid)
    }
}
