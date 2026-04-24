package com.apalashkin.sudoku.domain.model

class Board private constructor(private val cells: List<Cell>) {

    init {
        require(cells.size == SIZE * SIZE) { "board must have ${SIZE * SIZE} cells" }
    }

    fun cell(coord: Coord): Cell = cells[coord.row * SIZE + coord.col]

    fun withCell(coord: Coord, cell: Cell): Board {
        val updated = cells.toMutableList()
        updated[coord.row * SIZE + coord.col] = cell
        return Board(updated)
    }

    fun canPlace(coord: Coord, value: Int): Boolean {
        require(value in 1..9) { "value must be 1..9, was $value" }
        val boxRow = (coord.row / 3) * 3
        val boxCol = (coord.col / 3) * 3
        for (i in 0..8) {
            if (i != coord.col && cell(Coord(coord.row, i)).value == value) return false
            if (i != coord.row && cell(Coord(i, coord.col)).value == value) return false
        }
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if ((r != coord.row || c != coord.col) &&
                    cell(Coord(r, c)).value == value
                ) return false
            }
        }
        return true
    }

    companion object {
        const val SIZE = 9

        fun empty(): Board = Board(List(SIZE * SIZE) { Cell.empty() })

        fun fromGrid(grid: Array<IntArray>): Board {
            require(grid.size == SIZE) { "grid must have $SIZE rows, had ${grid.size}" }
            grid.forEachIndexed { i, row ->
                require(row.size == SIZE) { "row $i must have $SIZE cols, had ${row.size}" }
            }
            val cells = List(SIZE * SIZE) { idx ->
                val v = grid[idx / SIZE][idx % SIZE]
                when (v) {
                    0 -> Cell.empty()
                    in 1..9 -> Cell.given(v)
                    else -> throw IllegalArgumentException("grid value must be 0..9, was $v")
                }
            }
            return Board(cells)
        }
    }
}
