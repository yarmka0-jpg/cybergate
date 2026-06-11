package com.bth.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bth.launcher.data.AppInfo
import com.bth.launcher.data.AppRepository
import com.bth.launcher.data.LauncherTheme
import com.bth.launcher.data.SettingsStore
import com.bth.launcher.data.WeatherInfo
import com.bth.launcher.data.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LauncherUiState(
    val isLoading: Boolean = true,
    val allApps: List<AppInfo> = emptyList(),
    val visibleApps: List<AppInfo> = emptyList(),
    val favorites: List<AppInfo> = emptyList(),
    val mostUsed: List<AppInfo> = emptyList(),
    val theme: LauncherTheme = LauncherTheme.GLASS,
    val accentColor: String = "cyan",
    val gridColumns: Int = 4,
    val showLabels: Boolean = true,
    val hasPin: Boolean = false
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository(application)
    private val weatherRepository = WeatherRepository(application)
    val settings = SettingsStore(application)

    private val rawApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val isLoading = MutableStateFlow(true)

    private val _weather = MutableStateFlow<WeatherInfo?>(null)
    val weather: StateFlow<WeatherInfo?> = _weather

    private var lastWeatherFetchMs = 0L

    /** Theme/accent only — lets MainActivity avoid recomposing on every uiState change. */
    val themeState: StateFlow<LauncherTheme> = settings.theme
        .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherTheme.GLASS)

    val accentState: StateFlow<String> = settings.accentColor
        .stateIn(viewModelScope, SharingStarted.Eagerly, "cyan")

    /** Emitted when the Home button is pressed while the launcher is open. */
    private val _goHomeEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val goHomeEvents: SharedFlow<Unit> = _goHomeEvents

    private data class Prefs(
        val theme: LauncherTheme,
        val accent: String,
        val columns: Int,
        val labels: Boolean,
        val hasPin: Boolean
    )

    private val prefsFlow = combine(
        settings.theme,
        settings.accentColor,
        settings.gridColumns,
        settings.showLabels,
        settings.pinHash
    ) { theme, accent, columns, labels, pin ->
        Prefs(theme, accent, columns, labels, pin != null)
    }

    private val metaFlow = combine(
        prefsFlow,
        settings.launchCounts,
        isLoading
    ) { prefs, counts, loading ->
        Triple(prefs, counts, loading)
    }

    /**
     * Fully reactive state: every settings change (hide/lock/favorite/launch
     * count) flows through here automatically — no stale `.first()` reads.
     * All list work runs on Dispatchers.Default to keep the UI thread free.
     */
    val uiState: StateFlow<LauncherUiState> = combine(
        rawApps,
        settings.hiddenApps,
        settings.lockedApps,
        settings.favorites,
        metaFlow
    ) { apps, hidden, locked, favs, (prefs, counts, loading) ->
        val enriched = apps.map { app ->
            app.copy(
                isHidden = app.packageName in hidden,
                isLocked = app.packageName in locked,
                launchCount = counts[app.packageName] ?: 0
            )
        }
        val byPackage = enriched.associateBy { it.packageName }
        LauncherUiState(
            isLoading = loading && apps.isEmpty(),
            allApps = enriched,
            visibleApps = enriched.filter { !it.isHidden },
            favorites = favs.mapNotNull { byPackage[it] },
            mostUsed = enriched
                .filter { !it.isHidden && it.launchCount > 0 }
                .sortedByDescending { it.launchCount }
                .take(8),
            theme = prefs.theme,
            accentColor = prefs.accent,
            gridColumns = prefs.columns,
            showLabels = prefs.labels,
            hasPin = prefs.hasPin
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherUiState())

    init {
        refreshApps()
        refreshWeather()
    }

    fun refreshApps() {
        viewModelScope.launch {
            try {
                rawApps.value = appRepository.loadApps()
            } finally {
                isLoading.value = false
            }
        }
    }

    /** PERF: throttled — was firing a network request on every Home press. */
    fun refreshWeather(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && _weather.value != null &&
            now - lastWeatherFetchMs < WEATHER_REFRESH_INTERVAL_MS
        ) return
        lastWeatherFetchMs = now
        viewModelScope.launch {
            _weather.value = weatherRepository.fetchWeather() ?: _weather.value
        }
    }

    fun signalGoHome() {
        _goHomeEvents.tryEmit(Unit)
        refreshWeather()
    }

    fun onAppLaunched(packageName: String) {
        viewModelScope.launch { settings.incrementLaunchCount(packageName) }
    }

    fun toggleHidden(pkg: String) = viewModelScope.launch { settings.toggleHidden(pkg) }
    fun toggleLocked(pkg: String) = viewModelScope.launch { settings.toggleLocked(pkg) }
    fun toggleFavorite(pkg: String) = viewModelScope.launch { settings.toggleFavorite(pkg) }
    fun setTheme(theme: LauncherTheme) = viewModelScope.launch { settings.setTheme(theme) }
    fun setAccent(color: String) = viewModelScope.launch { settings.setAccentColor(color) }
    fun setGridColumns(c: Int) = viewModelScope.launch { settings.setGridColumns(c) }
    fun setShowLabels(s: Boolean) = viewModelScope.launch { settings.setShowLabels(s) }
    fun setPin(pin: String) = viewModelScope.launch { settings.setPin(pin) }
    fun clearPin() = viewModelScope.launch { settings.clearPin() }

    suspend fun verifyPin(pin: String): Boolean {
        val hash = settings.pinHash.first()
        return settings.verifyPin(pin, hash)
    }

    /** Smart search: prefix match first, then contains, then launch count. */
    fun search(query: String): List<AppInfo> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        return uiState.value.visibleApps
            .filter { it.name.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
            .sortedWith(
                compareByDescending<AppInfo> { it.name.lowercase().startsWith(q) }
                    .thenByDescending { it.launchCount }
                    .thenBy { it.name.lowercase() }
            )
    }

    companion object {
        private const val WEATHER_REFRESH_INTERVAL_MS = 15 * 60 * 1000L
    }
}
