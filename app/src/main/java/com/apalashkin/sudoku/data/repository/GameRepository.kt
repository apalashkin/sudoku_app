package com.apalashkin.sudoku.data.repository

import com.apalashkin.sudoku.data.db.GameDao
import com.apalashkin.sudoku.data.db.GameEntity
import com.apalashkin.sudoku.data.serialization.GameStateJsonCodec
import com.apalashkin.sudoku.ui.game.GameUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ActiveGame(val id: Long, val state: GameUiState)

class GameRepository(private val dao: GameDao) {

    fun observeActiveGame(): Flow<ActiveGame?> = dao.activeGame().map { entity ->
        entity?.let { ActiveGame(it.id, GameStateJsonCodec.decode(it.gameStateJson)) }
    }

    suspend fun startNewGame(state: GameUiState): Long {
        dao.deleteActiveGames()
        val now = System.currentTimeMillis()
        return dao.insert(
            GameEntity(
                difficulty = state.difficulty.name,
                gameStateJson = GameStateJsonCodec.encode(state),
                createdAt = now,
                updatedAt = now,
                completedAt = if (state.isComplete) now else null,
            )
        )
    }

    suspend fun saveState(id: Long, state: GameUiState) {
        val existing = dao.byId(id) ?: return
        val now = System.currentTimeMillis()
        dao.update(
            existing.copy(
                gameStateJson = GameStateJsonCodec.encode(state),
                difficulty = state.difficulty.name,
                updatedAt = now,
                completedAt = if (state.isComplete) now else null,
            )
        )
    }
}
