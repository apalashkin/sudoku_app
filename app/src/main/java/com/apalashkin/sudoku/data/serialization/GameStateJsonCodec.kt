package com.apalashkin.sudoku.data.serialization

import com.apalashkin.sudoku.domain.generator.Difficulty
import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Cell
import com.apalashkin.sudoku.domain.model.Coord
import com.apalashkin.sudoku.ui.game.GameUiState
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object GameStateJsonCodec {

    private val json = Json { ignoreUnknownKeys = true }

    fun encode(state: GameUiState): String = json.encodeToString(state.toDto())

    fun decode(payload: String): GameUiState =
        json.decodeFromString<GameStateDto>(payload).toModel()

    private fun GameUiState.toDto(): GameStateDto = GameStateDto(
        puzzle = puzzle.toCellList(),
        solution = solution.toCellList(),
        difficulty = difficulty.name,
        selected = selected?.let { CoordDto(it.row, it.col) },
        selectedDigit = selectedDigit,
        noteMode = noteMode,
        pencilMode = pencilMode,
        isComplete = isComplete,
        history = history.map { it.toCellList() },
        elapsedMs = elapsedMs,
        mistakeCount = mistakeCount,
        maxMistakes = maxMistakes,
    )

    private fun GameStateDto.toModel(): GameUiState = GameUiState(
        puzzle = puzzle.toBoard(),
        solution = solution.toBoard(),
        difficulty = Difficulty.valueOf(difficulty),
        selected = selected?.let { Coord(it.r, it.c) },
        selectedDigit = selectedDigit,
        noteMode = noteMode,
        pencilMode = pencilMode,
        isComplete = isComplete,
        history = history.map { it.toBoard() },
        elapsedMs = elapsedMs,
        mistakeCount = mistakeCount,
        maxMistakes = maxMistakes,
    )

    private fun Board.toCellList(): List<CellDto> =
        (0..8).flatMap { r ->
            (0..8).map { c ->
                val cell = cell(Coord(r, c))
                CellDto(v = cell.value, g = cell.isGiven, n = cell.notes.toList().sorted())
            }
        }

    private fun List<CellDto>.toBoard(): Board {
        require(size == 81) { "expected 81 cells, got $size" }
        var board = Board.empty()
        for ((idx, cellDto) in withIndex()) {
            val coord = Coord(idx / 9, idx % 9)
            board = board.withCell(coord, Cell(cellDto.v, cellDto.g, cellDto.n.toSet()))
        }
        return board
    }

    @Serializable
    private data class CellDto(
        val v: Int? = null,
        val g: Boolean = false,
        val n: List<Int> = emptyList(),
    )

    @Serializable
    private data class CoordDto(val r: Int, val c: Int)

    @Serializable
    private data class GameStateDto(
        val puzzle: List<CellDto>,
        val solution: List<CellDto>,
        val difficulty: String,
        val selected: CoordDto? = null,
        val selectedDigit: Int? = null,
        val noteMode: Boolean = false,
        val pencilMode: Boolean = false,
        val isComplete: Boolean = false,
        val history: List<List<CellDto>> = emptyList(),
        val elapsedMs: Long = 0L,
        val mistakeCount: Int = 0,
        val maxMistakes: Int = 3,
    )
}
