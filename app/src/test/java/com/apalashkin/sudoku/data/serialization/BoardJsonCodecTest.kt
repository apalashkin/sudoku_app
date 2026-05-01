package com.apalashkin.sudoku.data.serialization

import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Cell
import com.apalashkin.sudoku.domain.model.Coord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardJsonCodecTest {

    @Test
    fun `empty board round-trips`() {
        val board = Board.empty()
        val decoded = BoardJsonCodec.decode(BoardJsonCodec.encode(board))
        assertEquals(board, decoded)
    }

    @Test
    fun `board with given values round-trips`() {
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
        assertEquals(board, BoardJsonCodec.decode(BoardJsonCodec.encode(board)))
    }

    @Test
    fun `user-filled and noted cells round-trip`() {
        val board = Board.empty()
            .withCell(Coord(0, 0), Cell.filled(7))
            .withCell(Coord(1, 1), Cell.empty().copy(notes = setOf(1, 4, 9)))
        val decoded = BoardJsonCodec.decode(BoardJsonCodec.encode(board))
        assertEquals(board, decoded)
        assertEquals(setOf(1, 4, 9), decoded.cell(Coord(1, 1)).notes)
        assertEquals(7, decoded.cell(Coord(0, 0)).value)
    }

    @Test
    fun `decode rejects malformed payload`() {
        assertThrows(Exception::class.java) { BoardJsonCodec.decode("not json") }
    }

    @Test
    fun `decode rejects wrong cell count`() {
        val tooFew = """{"cells":[{"v":1,"g":false,"n":[]}]}"""
        assertThrows(IllegalArgumentException::class.java) {
            BoardJsonCodec.decode(tooFew)
        }
    }

    @Test
    fun `encoded JSON is non-empty`() {
        assertTrue(BoardJsonCodec.encode(Board.empty()).isNotEmpty())
    }
}
