package com.apalashkin.sudoku.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.apalashkin.sudoku.data.db.AppDatabase
import com.apalashkin.sudoku.data.repository.ActiveGame
import com.apalashkin.sudoku.data.repository.GameRepository
import com.apalashkin.sudoku.domain.generator.Difficulty
import com.apalashkin.sudoku.domain.generator.PuzzleGenerator
import com.apalashkin.sudoku.ui.game.GameUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: GameRepository,
    private val generator: PuzzleGenerator = PuzzleGenerator(),
) : ViewModel() {

    val activeGame: StateFlow<ActiveGame?> = repository.observeActiveGame()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun startNewGame(difficulty: Difficulty, onReady: () -> Unit) {
        viewModelScope.launch {
            val puzzle = generator.generate(difficulty)
            val state = GameUiState.fromPuzzle(puzzle)
            repository.startNewGame(state)
            onReady()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as Application
                val db = AppDatabase.get(app)
                HomeViewModel(GameRepository(db.gameDao()))
            }
        }
    }
}
