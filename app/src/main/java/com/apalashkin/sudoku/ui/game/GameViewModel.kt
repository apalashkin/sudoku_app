package com.apalashkin.sudoku.ui.game

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.apalashkin.sudoku.data.db.AppDatabase
import com.apalashkin.sudoku.data.repository.GameRepository
import com.apalashkin.sudoku.domain.model.Coord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<GameUiState?>(null)
    val state: StateFlow<GameUiState?> = _state.asStateFlow()

    private var currentGameId: Long? = null

    init {
        viewModelScope.launch {
            val active = repository.observeActiveGame().first()
            if (active != null) {
                currentGameId = active.id
                _state.value = active.state
            }
        }
    }

    fun selectCell(coord: Coord) = mutate { it.selectCell(coord) }
    fun selectDigit(digit: Int) = mutate { it.selectDigit(digit) }
    fun erase() = mutate { it.erase() }
    fun toggleNoteMode() = mutate { it.toggleNoteMode() }
    fun undo() = mutate { it.undo() }
    fun tick(deltaMs: Long) = mutate { it.tick(deltaMs) }

    private fun mutate(transform: (GameUiState) -> GameUiState) {
        val current = _state.value ?: return
        val updated = transform(current)
        if (updated === current) return
        _state.value = updated
        val id = currentGameId ?: return
        viewModelScope.launch { repository.saveState(id, updated) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as Application
                val db = AppDatabase.get(app)
                GameViewModel(GameRepository(db.gameDao()))
            }
        }
    }
}
