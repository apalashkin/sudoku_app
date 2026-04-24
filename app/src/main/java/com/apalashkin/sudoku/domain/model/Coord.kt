package com.apalashkin.sudoku.domain.model

data class Coord(val row: Int, val col: Int) {
    init {
        require(row in 0..8) { "row must be in 0..8, was $row" }
        require(col in 0..8) { "col must be in 0..8, was $col" }
    }

    val box: Int get() = (row / 3) * 3 + (col / 3)
}
