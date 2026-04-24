package com.apalashkin.sudoku.domain.model

data class Cell(
    val value: Int? = null,
    val isGiven: Boolean = false,
    val notes: Set<Int> = emptySet(),
) {
    init {
        value?.let { require(it in 1..9) { "value must be 1..9, was $it" } }
        notes.forEach { require(it in 1..9) { "note must be 1..9, was $it" } }
        require(notes.isEmpty() || value == null) { "cell with value cannot hold notes" }
    }

    val isEmpty: Boolean get() = value == null

    companion object {
        fun empty(): Cell = Cell()
        fun given(value: Int): Cell = Cell(value = value, isGiven = true)
        fun filled(value: Int): Cell = Cell(value = value, isGiven = false)
    }
}
