package com.apalashkin.sudoku.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val difficulty: String,
    val gameStateJson: String,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
)
