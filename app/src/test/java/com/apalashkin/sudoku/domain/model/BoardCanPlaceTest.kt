package com.apalashkin.sudoku.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardCanPlaceTest {

    @Test
    fun `any digit can be placed on empty board`() {
        val b = Board.empty()
        for (d in 1..9) assertTrue(b.canPlace(Coord(0, 0), d))
    }

    @Test
    fun `canPlace rejects value outside 1 to 9`() {
        val b = Board.empty()
        assertThrows(IllegalArgumentException::class.java) { b.canPlace(Coord(0, 0), 0) }
        assertThrows(IllegalArgumentException::class.java) { b.canPlace(Coord(0, 0), 10) }
    }

    @Test
    fun `canPlace rejects duplicate in same row`() {
        val b = Board.empty().withCell(Coord(0, 0), Cell.filled(5))
        assertFalse(b.canPlace(Coord(0, 5), 5))
    }

    @Test
    fun `canPlace rejects duplicate in same column`() {
        val b = Board.empty().withCell(Coord(0, 0), Cell.filled(5))
        assertFalse(b.canPlace(Coord(5, 0), 5))
    }

    @Test
    fun `canPlace rejects duplicate in same 3x3 box`() {
        val b = Board.empty().withCell(Coord(0, 0), Cell.filled(5))
        assertFalse(b.canPlace(Coord(1, 1), 5))
        assertFalse(b.canPlace(Coord(2, 2), 5))
    }

    @Test
    fun `canPlace allows value in different row col and box`() {
        val b = Board.empty().withCell(Coord(0, 0), Cell.filled(5))
        assertTrue(b.canPlace(Coord(3, 3), 5))
    }

    @Test
    fun `canPlace ignores the cell's own current value`() {
        val b = Board.empty().withCell(Coord(0, 0), Cell.filled(5))
        assertTrue(b.canPlace(Coord(0, 0), 5))
    }

    @Test
    fun `canPlace allows a digit not used by any peer`() {
        val b = Board.empty().withCell(Coord(0, 0), Cell.filled(5))
        assertTrue(b.canPlace(Coord(0, 0), 7))
    }
}
