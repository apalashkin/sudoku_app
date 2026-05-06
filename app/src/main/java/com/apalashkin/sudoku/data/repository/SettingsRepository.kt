package com.apalashkin.sudoku.data.repository

import com.apalashkin.sudoku.data.db.SettingsDao
import com.apalashkin.sudoku.data.db.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val maxMistakes: Int = 3,
)

class SettingsRepository(private val dao: SettingsDao) {

    fun observe(): Flow<AppSettings> = combine(
        dao.observe(KEY_THEME_MODE),
        dao.observe(KEY_MAX_MISTAKES),
    ) { theme, maxMistakes ->
        AppSettings(
            themeMode = theme?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            maxMistakes = maxMistakes?.toIntOrNull()?.takeIf { it > 0 } ?: 3,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dao.put(SettingsEntity(KEY_THEME_MODE, mode.name))
    }

    suspend fun setMaxMistakes(value: Int) {
        require(value > 0) { "maxMistakes must be > 0, was $value" }
        dao.put(SettingsEntity(KEY_MAX_MISTAKES, value.toString()))
    }

    private companion object {
        const val KEY_THEME_MODE = "themeMode"
        const val KEY_MAX_MISTAKES = "maxMistakes"
    }
}
