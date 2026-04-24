package com.apalashkin.sudoku.domain.generator

import com.apalashkin.sudoku.domain.model.Board
import kotlin.random.Random

class PuzzleGenerator(private val random: Random = Random.Default) {

    fun generate(difficulty: Difficulty): Puzzle {
        val solution = generateCompleteGrid()
        val puzzle = dig(solution, targetClues = difficulty.targetClues)
        return Puzzle(
            puzzle = Board.fromGrid(toIntGrid(puzzle)),
            solution = Board.fromGrid(toIntGrid(solution)),
            difficulty = difficulty,
        )
    }

    private fun generateCompleteGrid(): IntArray {
        val grid = IntArray(81)
        fillRecursive(grid, 0)
        return grid
    }

    private fun fillRecursive(grid: IntArray, idx: Int): Boolean {
        if (idx == 81) return true
        val digits = (1..9).shuffled(random)
        for (v in digits) {
            if (Solver.isValid(grid, idx / 9, idx % 9, v)) {
                grid[idx] = v
                if (fillRecursive(grid, idx + 1)) return true
                grid[idx] = 0
            }
        }
        return false
    }

    private fun dig(solution: IntArray, targetClues: Int): IntArray {
        val puzzle = solution.copyOf()
        val positions = (0 until 81).shuffled(random)
        var clues = 81
        for (idx in positions) {
            if (clues <= targetClues) break
            val backup = puzzle[idx]
            puzzle[idx] = 0
            if (Solver.countSolutions(puzzle, limit = 2) == 1) {
                clues--
            } else {
                puzzle[idx] = backup
            }
        }
        return puzzle
    }

    private fun toIntGrid(flat: IntArray): Array<IntArray> =
        Array(9) { r -> IntArray(9) { c -> flat[r * 9 + c] } }
}
