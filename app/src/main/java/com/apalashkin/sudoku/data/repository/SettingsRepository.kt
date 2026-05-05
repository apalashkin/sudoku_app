package com.apalashkin.sudoku.data.repository

import com.apalashkin.sudoku.data.db.SettingsDao
import com.apalashkin.sudoku.data.db.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

class SettingsRepository(private val dao: SettingsDao) {

    fun observe(): Flow<AppSettings> = dao.observe(KEY_THEME_MODE).map { raw ->
        AppSettings(
            themeMode = raw?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dao.put(SettingsEntity(KEY_THEME_MODE, mode.name))
    }

    private companion object {
        const val KEY_THEME_MODE = "themeMode"
    }
}
