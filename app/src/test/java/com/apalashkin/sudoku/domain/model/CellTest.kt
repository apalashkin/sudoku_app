package com.apalashkin.sudoku.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CellTest {

    @Test
    fun `empty cell has null value, not given, no notes`() {
        val c = Cell.empty()
        assertEquals(null, c.value)
        assertFalse(c.isGiven)
        assertTrue(c.notes.isEmpty())
        assertTrue(c.isEmpty)
    }

    @Test
    fun `given cell carries value and is marked given`() {
        val c = Cell.given(5)
        assertEquals(5, c.value)
        assertTrue(c.isGiven)
        assertFalse(c.isEmpty)
    }

    @Test
    fun `user-filled cell has value but is not given`() {
        val c = Cell.filled(7)
        assertEquals(7, c.value)
        assertFalse(c.isGiven)
        assertFalse(c.isEmpty)
    }

    @Test
    fun `cell value must be 1 to 9`() {
        assertThrows(IllegalArgumentException::class.java) { Cell.filled(0) }
        assertThrows(IllegalArgumentException::class.java) { Cell.filled(10) }
        assertThrows(IllegalArgumentException::class.java) { Cell.given(0) }
        assertThrows(IllegalArgumentException::class.java) { Cell.given(-1) }
    }

    @Test
    fun `notes must be digits 1 to 9`() {
        assertThrows(IllegalArgumentException::class.java) {
            Cell.empty().copy(notes = setOf(0))
        }
        assertThrows(IllegalArgumentException::class.java) {
            Cell.empty().copy(notes = setOf(10))
        }
    }

    @Test
    fun `given cell cannot hold notes`() {
        assertThrows(IllegalArgumentException::class.java) {
            Cell.given(3).copy(notes = setOf(1, 2))
        }
    }

    @Test
    fun `cell with value has no notes`() {
        assertThrows(IllegalArgumentException::class.java) {
            Cell.filled(4).copy(notes = setOf(1, 2))
        }
    }
}
