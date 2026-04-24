package com.apalashkin.sudoku.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CoordTest {

    @Test
    fun `coord stores row and col`() {
        val c = Coord(row = 3, col = 5)
        assertEquals(3, c.row)
        assertEquals(5, c.col)
    }

    @Test
    fun `coord accepts corners of a 9x9 grid`() {
        Coord(0, 0)
        Coord(8, 8)
        Coord(0, 8)
        Coord(8, 0)
    }

    @Test
    fun `coord rejects negative row`() {
        assertThrows(IllegalArgumentException::class.java) { Coord(-1, 0) }
    }

    @Test
    fun `coord rejects row above 8`() {
        assertThrows(IllegalArgumentException::class.java) { Coord(9, 0) }
    }

    @Test
    fun `coord rejects negative col`() {
        assertThrows(IllegalArgumentException::class.java) { Coord(0, -1) }
    }

    @Test
    fun `coord rejects col above 8`() {
        assertThrows(IllegalArgumentException::class.java) { Coord(0, 9) }
    }

    @Test
    fun `coord identifies its 3x3 box`() {
        assertEquals(0, Coord(0, 0).box)
        assertEquals(0, Coord(2, 2).box)
        assertEquals(1, Coord(0, 3).box)
        assertEquals(4, Coord(4, 4).box)
        assertEquals(8, Coord(8, 8).box)
    }
}
