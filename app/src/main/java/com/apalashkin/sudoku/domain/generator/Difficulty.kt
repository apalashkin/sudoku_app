package com.apalashkin.sudoku.domain.generator

enum class Difficulty(val targetClues: Int) {
    EASY(42),
    MEDIUM(34),
    HARD(30),
    EXPERT(26),
}
