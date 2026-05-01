package com.apalashkin.sudoku.data.serialization

import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Cell
import com.apalashkin.sudoku.domain.model.Coord
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BoardJsonCodec {

    private val json = Json { ignoreUnknownKeys = true }

    fun encode(board: Board): String {
        val cells = (0..8).flatMap { r ->
            (0..8).map { c ->
                val cell = board.cell(Coord(r, c))
                CellDto(v = cell.value, g = cell.isGiven, n = cell.notes.toList().sorted())
            }
        }
        return json.encodeToString(BoardDto(cells))
    }

    fun decode(payload: String): Board {
        val dto = json.decodeFromString<BoardDto>(payload)
        require(dto.cells.size == 81) { "expected 81 cells, got ${dto.cells.size}" }
        var board = Board.empty()
        for ((idx, cellDto) in dto.cells.withIndex()) {
            val coord = Coord(idx / 9, idx % 9)
            val cell = Cell(
                value = cellDto.v,
                isGiven = cellDto.g,
                notes = cellDto.n.toSet(),
            )
            board = board.withCell(coord, cell)
        }
        return board
    }

    @Serializable
    private data class BoardDto(val cells: List<CellDto>)

    @Serializable
    private data class CellDto(
        val v: Int? = null,
        val g: Boolean = false,
        val n: List<Int> = emptyList(),
    )
}
