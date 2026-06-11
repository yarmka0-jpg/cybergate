package com.bth.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore by preferencesDataStore(name = "launcher_settings")

class SettingsStore(private val context: Context) {

    companion object {
        private val THEME = stringPreferencesKey("theme")
        private val ACCENT = stringPreferencesKey("accent_color")
        private val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        private val LOCKED_APPS = stringSetPreferencesKey("locked_apps")
        // Ordered (comma-separated) so dock icons never jump around
        private val FAVORITES_ORDERED = stringPreferencesKey("dock_favorites_ordered")
        private val PIN_HASH = stringPreferencesKey("pin_hash")
        private val GRID_COLUMNS = intPreferencesKey("grid_columns")
        private val SHOW_LABELS = booleanPreferencesKey("show_labels")
        private val LAUNCH_COUNTS = stringPreferencesKey("launch_counts")
        private const val MAX_FAVORITES = 5
    }

    val theme: Flow<LauncherTheme> = context.dataStore.data.map { prefs ->
        try {
            LauncherTheme.valueOf(prefs[THEME] ?: LauncherTheme.GLASS.name)
        } catch (e: Exception) {
            LauncherTheme.GLASS
        }
    }

    val accentColor: Flow<String> = context.dataStore.data.map { it[ACCENT] ?: "cyan" }
    val hiddenApps: Flow<Set<String>> = context.dataStore.data.map { it[HIDDEN_APPS] ?: emptySet() }
    val lockedApps: Flow<Set<String>> = context.dataStore.data.map { it[LOCKED_APPS] ?: emptySet() }

    val favorites: Flow<List<String>> = context.dataStore.data.map { prefs ->
        (prefs[FAVORITES_ORDERED] ?: "")
            .split(",")
            .filter { it.isNotBlank() }
    }

    val pinHash: Flow<String?> = context.dataStore.data.map { it[PIN_HASH] }
    val gridColumns: Flow<Int> = context.dataStore.data.map { it[GRID_COLUMNS] ?: 4 }
    val showLabels: Flow<Boolean> = context.dataStore.data.map { it[SHOW_LABELS] ?: true }

    val launchCounts: Flow<Map<String, Int>> = context.dataStore.data.map { prefs ->
        parseCounts(prefs[LAUNCH_COUNTS] ?: "")
    }

    suspend fun setTheme(theme: LauncherTheme) =
        context.dataStore.edit { it[THEME] = theme.name }

    suspend fun setAccentColor(color: String) =
        context.dataStore.edit { it[ACCENT] = color }

    suspend fun toggleHidden(pkg: String) = context.dataStore.edit { prefs ->
        val current = prefs[HIDDEN_APPS] ?: emptySet()
        prefs[HIDDEN_APPS] = if (pkg in current) current - pkg else current + pkg
    }

    suspend fun toggleLocked(pkg: String) = context.dataStore.edit { prefs ->
        val current = prefs[LOCKED_APPS] ?: emptySet()
        prefs[LOCKED_APPS] = if (pkg in current) current - pkg else current + pkg
    }

    suspend fun toggleFavorite(pkg: String) = context.dataStore.edit { prefs ->
        val current = (prefs[FAVORITES_ORDERED] ?: "")
            .split(",")
            .filter { it.isNotBlank() }
        val updated = if (pkg in current) {
            current - pkg
        } else {
            (current + pkg).takeLast(MAX_FAVORITES)
        }
        prefs[FAVORITES_ORDERED] = updated.joinToString(",")
    }

    suspend fun setPin(pin: String) =
        context.dataStore.edit { it[PIN_HASH] = sha256(pin) }

    suspend fun clearPin() =
        context.dataStore.edit { it.remove(PIN_HASH) }

    fun verifyPin(pin: String, storedHash: String?): Boolean =
        storedHash != null && sha256(pin) == storedHash

    suspend fun setGridColumns(columns: Int) =
        context.dataStore.edit { it[GRID_COLUMNS] = columns.coerceIn(3, 6) }

    suspend fun setShowLabels(show: Boolean) =
        context.dataStore.edit { it[SHOW_LABELS] = show }

    suspend fun incrementLaunchCount(pkg: String) = context.dataStore.edit { prefs ->
        val counts = parseCounts(prefs[LAUNCH_COUNTS] ?: "").toMutableMap()
        counts[pkg] = (counts[pkg] ?: 0) + 1
        prefs[LAUNCH_COUNTS] = counts.entries
            .sortedByDescending { it.value }
            .take(50)
            .joinToString(";") { "${it.key}=${it.value}" }
    }

    private fun parseCounts(raw: String): Map<String, Int> =
        raw.split(";")
            .mapNotNull { entry ->
                val parts = entry.split("=")
                if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 0) else null
            }
            .toMap()

    private fun sha256(input: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
