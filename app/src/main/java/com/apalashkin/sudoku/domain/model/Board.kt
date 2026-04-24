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
