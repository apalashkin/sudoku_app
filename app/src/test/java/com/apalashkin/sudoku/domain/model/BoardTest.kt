package com.apalashkin.sudoku.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    @Test
    fun `empty board has 81 empty cells`() {
        val b = Board.empty()
        for (r in 0..8) for (c in 0..8) {
            assertTrue(b.cell(Coord(r, c)).isEmpty)
        }
    }

    @Test
    fun `fromGrid marks non-zero values as given`() {
        val grid = Array(9) { IntArray(9) }
        grid[0][0] = 5
        grid[4][4] = 3
        val b = Board.fromGrid(grid)

        val c00 = b.cell(Coord(0, 0))
        assertEquals(5, c00.value)
        assertTrue(c00.isGiven)

        val c44 = b.cell(Coord(4, 4))
        assertEquals(3, c44.value)
        assertTrue(c44.isGiven)

        assertTrue(b.cell(Coord(1, 1)).isEmpty)
    }

    @Test
    fun `fromGrid zero means empty`() {
        val b = Board.fromGrid(Array(9) { IntArray(9) })
        assertTrue(b.cell(Coord(0, 0)).isEmpty)
        assertFalse(b.cell(Coord(0, 0)).isGiven)
    }

    @Test
    fun `fromGrid rejects non-9x9 input`() {
        assertThrows(IllegalArgumentException::class.java) {
            Board.fromGrid(Array(8) { IntArray(9) })
        }
        assertThrows(IllegalArgumentException::class.java) {
            Board.fromGrid(Array(9) { IntArray(8) })
        }
    }

    @Test
    fun `fromGrid rejects out-of-range values`() {
        val bad = Array(9) { IntArray(9) }
        bad[0][0] = 10
        assertThrows(IllegalArgumentException::class.java) { Board.fromGrid(bad) }

        val neg = Array(9) { IntArray(9) }
        neg[0][0] = -1
        assertThrows(IllegalArgumentException::class.java) { Board.fromGrid(neg) }
    }

    @Test
    fun `withCell returns a new board with the cell swapped`() {
        val b = Board.empty()
        val updated = b.withCell(Coord(2, 3), Cell.filled(7))

        assertEquals(7, updated.cell(Coord(2, 3)).value)
        assertTrue(b.cell(Coord(2, 3)).isEmpty)
    }

    @Test
    fun `withCell leaves other cells untouched`() {
        val start = Board.empty().withCell(Coord(0, 0), Cell.filled(1))
        val updated = start.withCell(Coord(8, 8), Cell.filled(9))

        assertEquals(1, updated.cell(Coord(0, 0)).value)
        assertEquals(9, updated.cell(Coord(8, 8)).value)
    }
}
