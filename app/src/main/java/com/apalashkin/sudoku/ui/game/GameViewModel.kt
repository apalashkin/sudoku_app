package com.apalashkin.sudoku.ui.game

import androidx.lifecycle.ViewModel
import com.apalashkin.sudoku.domain.generator.Difficulty
import com.apalashkin.sudoku.domain.generator.PuzzleGenerator
import com.apalashkin.sudoku.domain.model.Coord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(
    private val generator: PuzzleGenerator = PuzzleGenerator(),
    initialDifficulty: Difficulty = Difficulty.EASY,
) : ViewModel() {

    private val _state = MutableStateFlow(
        GameUiState.fromPuzzle(generator.generate(initialDifficulty))
    )
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    fun selectCell(coord: Coord) {
        _state.value = _state.value.selectCell(coord)
    }

    fun placeDigit(digit: Int) {
        _state.value = _state.value.placeDigit(digit)
    }

    fun erase() {
        _state.value = _state.value.erase()
    }

    fun toggleNoteMode() {
        _state.value = _state.value.toggleNoteMode()
    }

    fun undo() {
        _state.value = _state.value.undo()
    }

    fun newGame(difficulty: Difficulty) {
        _state.value = GameUiState.fromPuzzle(generator.generate(difficulty))
    }
}
