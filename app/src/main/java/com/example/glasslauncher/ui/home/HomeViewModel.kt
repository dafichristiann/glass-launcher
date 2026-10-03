package com.example.glasslauncher.ui.home

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glasslauncher.core.device.DeviceProfile
import com.example.glasslauncher.core.glass.GlassStrategy
import com.example.glasslauncher.core.icons.GlassIconRenderer
import com.example.glasslauncher.core.icons.GlassIconRequest
import com.example.glasslauncher.core.icons.IconCache
import com.example.glasslauncher.core.icons.cacheKey
import com.example.glasslauncher.core.wallpaper.BlurredWallpaperCache
import com.example.glasslauncher.core.wallpaper.WallpaperRepository
import com.example.glasslauncher.data.apps.AppEntry
import com.example.glasslauncher.data.apps.AppRepository
import com.example.glasslauncher.data.workspace.DefaultLayout
import com.example.glasslauncher.data.workspace.WorkspaceItem
import com.example.glasslauncher.data.workspace.WorkspaceRepository
import com.example.glasslauncher.ui.widgets.WeatherUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Home screen state owner (PRD section 5). Merges the wallpaper, installed apps, and workspace
 * layout into one UI state; renders glass icons off the main thread into [IconCache].
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val appRepository: AppRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val iconCache: IconCache,
    private val iconRenderer: GlassIconRenderer,
    private val deviceProfile: DeviceProfile,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val density: Float = appContext.resources.displayMetrics.density

    private val _weather = MutableStateFlow(WeatherUiState.unavailable("Jakarta"))
    val weather: StateFlow<WeatherUiState> = _weather.asStateFlow()

    private val _wallpaperBitmap = MutableStateFlow<Bitmap?>(null)
    val wallpaper: StateFlow<Bitmap?> = _wallpaperBitmap.asStateFlow()

    /** Blurred wallpaper for Strategy A (medium intensity). */
    private val _blurredWallpaper = MutableStateFlow<Bitmap?>(null)
    val blurredWallpaper: StateFlow<Bitmap?> = _blurredWallpaper.asStateFlow()

    private val _iconBitmaps = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val iconBitmaps: StateFlow<Map<String, Bitmap>> = _iconBitmaps.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    val today: LocalDate get() = LocalDate.now()

    val strategy: GlassStrategy = GlassStrategy.Precomputed

    val apps: StateFlow<List<AppEntry>> = appRepository.apps
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val desktopByPage: StateFlow<Map<Int, List<WorkspaceItem>>> = workspaceRepository.desktopByPage
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val dockItems: StateFlow<List<WorkspaceItem>> = workspaceRepository.dockItems
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        appRepository.start()
        loadWallpaper()
        viewModelScope.launch {
            // Render icons whenever the app list changes, and seed the dock once apps exist.
            apps.collect { list ->
                if (list.isNotEmpty()) {
                    renderIcons(list)
                    seedDockIfEmpty(list)
                    seedPageTwoIfEmpty(list)
                }
            }
        }
    }

    private fun loadWallpaper() {
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.Default) { wallpaperRepository.designWallpaper() }
            _wallpaperBitmap.value = bitmap

            val medium = withContext(Dispatchers.Default) {
                BlurredWallpaperCache(bitmap).blurred(BlurredWallpaperCache.BlurIntensity.Medium)
            }
            _blurredWallpaper.value = medium

            // Seed the static widget layout immediately (does not depend on apps).
            workspaceRepository.ensureWidgetsInitialized(DefaultLayout.widgetsOnly(deviceProfile))
        }
    }

    /**
     * The dock holds real apps, so it can only be seeded after the app list loads. We seed it
     * once, when it is still empty, using the resolved role apps (PRD section 2 dock defaults).
     */
    private suspend fun seedDockIfEmpty(list: List<AppEntry>) {
        if (workspaceRepository.dockCount() > 0) return
        val dockDefaults = deviceProfile.dockApps.mapNotNull { pkg ->
            list.firstOrNull { it.packageName == pkg }?.let { it.packageName to it.componentName }
        }
        val fallback = if (dockDefaults.isEmpty()) {
            // Device gave no role apps: fall back to the first four launchable apps.
            list.take(deviceProfile.gridColumns).map { it.packageName to it.componentName }
        } else {
            dockDefaults
        }
        workspaceRepository.ensureDockInitialized(DefaultLayout.dockOnly(deviceProfile, fallback))
    }

    /** Seed page 2 with the app grid once, when no desktop app items exist yet (PRD section 9.2). */
    private suspend fun seedPageTwoIfEmpty(list: List<AppEntry>) {
        if (workspaceRepository.desktopAppCount() > 0) return
        workspaceRepository.ensurePageTwoInitialized(DefaultLayout.pageTwoApps(deviceProfile, list))
    }

    private suspend fun renderIcons(list: List<AppEntry>) {
        val rendered = withContext(Dispatchers.Default) {
            list.associate { app ->
                val request = GlassIconRequest(
                    packageName = app.packageName,
                    componentName = app.componentName,
                    userSerial = app.userSerial,
                )
                val key = request.cacheKey()
                val cached = iconCache.get(key)
                val bitmap = cached ?: iconRenderer.render(app.icon, request, density).also {
                    iconCache.put(key, it)
                }
                app.key to bitmap
            }
        }
        _iconBitmaps.value = rendered
    }

    fun previousMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }

    fun nextMonth() { _currentMonth.value = _currentMonth.value.plusMonths(1) }

    fun openApp(app: AppEntry) = appRepository.launch(app)

    fun openDockItem(item: WorkspaceItem) {
        val entry = apps.value.firstOrNull {
            it.componentName == item.componentName && it.packageName == item.packageName
        } ?: return
        openApp(entry)
    }

    fun openCalendar(date: LocalDate) {
        // Phase 3+ wires ACTION_VIEW to the default calendar.
    }

    override fun onCleared() {
        appRepository.stop()
        super.onCleared()
    }
}
