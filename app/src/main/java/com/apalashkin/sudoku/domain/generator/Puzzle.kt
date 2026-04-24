package com.apalashkin.sudoku.domain.generator

import com.apalashkin.sudoku.domain.model.Board

data class Puzzle(
    val puzzle: Board,
    val solution: Board,
    val difficulty: Difficulty,
)
