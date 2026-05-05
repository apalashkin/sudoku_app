package com.apalashkin.sudoku.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM games WHERE completedAt IS NULL ORDER BY updatedAt DESC LIMIT 1")
    fun activeGame(): Flow<GameEntity?>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun byId(id: Long): GameEntity?

    @Insert
    suspend fun insert(game: GameEntity): Long

    @Update
    suspend fun update(game: GameEntity)

    @Query("DELETE FROM games WHERE completedAt IS NULL")
    suspend fun deleteActiveGames()

    @Query("SELECT * FROM games WHERE completedAt IS NOT NULL ORDER BY completedAt DESC")
    fun completedGames(): Flow<List<GameEntity>>
}
