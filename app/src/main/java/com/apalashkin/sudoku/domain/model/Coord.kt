package com.apalashkin.sudoku.domain.model

data class Coord(val row: Int, val col: Int) {
    init {
        require(row in 0..8) { "row must be in 0..8, was $row" }
        require(col in 0..8) { "col must be in 0..8, was $col" }
    }

    val box: Int get() = (row / 3) * 3 + (col / 3)

    fun peers(): Set<Coord> {
        val result = mutableSetOf<Coord>()
        for (i in 0..8) {
            if (i != col) result += Coord(row, i)
            if (i != row) result += Coord(i, col)
        }
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (r != row || c != col) result += Coord(r, c)
            }
        }
        return result
    }
}
