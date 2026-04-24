package com.apalashkin.sudoku.domain.generator

import com.apalashkin.sudoku.domain.model.Board

sealed interface SolveResult {
    data object None : SolveResult
    data class Unique(val solution: Board) : SolveResult
    data object Multiple : SolveResult
}
