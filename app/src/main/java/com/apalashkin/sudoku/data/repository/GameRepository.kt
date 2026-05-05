package com.apalashkin.sudoku.data.repository

import com.apalashkin.sudoku.data.db.GameDao
import com.apalashkin.sudoku.data.db.GameEntity
import com.apalashkin.sudoku.data.serialization.GameStateJsonCodec
import com.apalashkin.sudoku.domain.generator.Difficulty
import com.apalashkin.sudoku.ui.game.GameUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ActiveGame(val id: Long, val state: GameUiState)

data class DifficultyStats(val total: Int, val bestTimeMs: Long?)
data class Stats(val byDifficulty: Map<Difficulty, DifficultyStats>) {
    companion object {
        val Empty = Stats(emptyMap())
    }
}

class GameRepository(private val dao: GameDao) {

    fun observeActiveGame(): Flow<ActiveGame?> = dao.activeGame().map { entity ->
        entity?.let { ActiveGame(it.id, GameStateJsonCodec.decode(it.gameStateJson)) }
    }

    fun observeStats(): Flow<Stats> = dao.completedGames().map { entities ->
        val grouped = mutableMapOf<Difficulty, MutableList<Long>>()
        for (entity in entities) {
            val difficulty = runCatching { Difficulty.valueOf(entity.difficulty) }.getOrNull() ?: continue
            val state = runCatching { GameStateJsonCodec.decode(entity.gameStateJson) }.getOrNull() ?: continue
            grouped.getOrPut(difficulty) { mutableListOf() }.add(state.elapsedMs)
        }
        Stats(byDifficulty = grouped.mapValues { (_, times) ->
            val timed = times.filter { it > 0 }
            DifficultyStats(
                total = times.size,
                bestTimeMs = if (timed.isEmpty()) null else timed.min(),
            )
        })
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
