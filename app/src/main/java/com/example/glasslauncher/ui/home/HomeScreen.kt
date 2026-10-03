package com.example.glasslauncher.ui.home

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glasslauncher.core.glass.GlassTokens
import com.example.glasslauncher.data.apps.AppEntry
import com.example.glasslauncher.data.workspace.DefaultLayout
import com.example.glasslauncher.data.workspace.ItemType
import com.example.glasslauncher.data.workspace.WorkspaceItem
import com.example.glasslauncher.ui.icon.AppIcon
import com.example.glasslauncher.ui.widgets.DateWidget
import com.example.glasslauncher.ui.widgets.MonthCalendarWidget
import com.example.glasslauncher.ui.widgets.WeatherUiState
import com.example.glasslauncher.ui.widgets.WeatherWidget
import com.example.glasslauncher.ui.widgets.WidgetWallpaperState
import java.time.YearMonth

/**
 * Home screen (PRD sections 9.1 and 9.2): a pager of pages, each rendering built-in widgets and
 * an app grid from the workspace layout, plus a shared dock and page indicator.
 */
@Composable
fun HomeScreen(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val desktopByPage by viewModel.desktopByPage.collectAsStateWithLifecycle()
    val dockItems by viewModel.dockItems.collectAsStateWithLifecycle()
    val icons by viewModel.iconBitmaps.collectAsStateWithLifecycle()
    val blurred by viewModel.blurredWallpaper.collectAsStateWithLifecycle()
    val wallpaperBitmap by viewModel.wallpaper.collectAsStateWithLifecycle()
    val month by viewModel.currentMonth.collectAsStateWithLifecycle()
    val weather by viewModel.weather.collectAsStateWithLifecycle()

    val screenWidthPx = (wallpaperBitmap?.width ?: 0)
    val screenHeightPx = (wallpaperBitmap?.height ?: 0)
    val wallpaperState = WidgetWallpaperState(
        image = blurred?.asImageBitmap(),
        screenSize = IntSize(screenWidthPx, screenHeightPx),
        strategy = viewModel.strategy,
    )

    val pageCount = maxOf(1, (desktopByPage.keys.maxOrNull() ?: 0) + 1)
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Box(modifier = modifier.fillMaxSize()) {
        // The launcher draws its own wallpaper (PRD section 8, mode 1).
        wallpaperBitmap?.let { bmp ->
            androidx.compose.foundation.Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    HomePage(
                        page = page,
                        items = desktopByPage[page].orEmpty(),
                        apps = apps,
                        icons = icons,
                        month = month,
                        weather = weather,
                        wallpaperState = wallpaperState,
                        viewModel = viewModel,
                        modifier = Modifier.padding(
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                        ),
                    )
                }

                PageIndicator(
                    pageCount = pageCount,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                )
            }

            Dock(
                slots = dockItems.map { item ->
                    DockSlot(
                        label = labelFor(item, apps),
                        icon = iconFor(item, apps, icons),
                        onClick = { viewModel.openDockItem(item) },
                    )
                },
                wallpaper = wallpaperState,
                modifier = Modifier.padding(
                    start = GlassTokens.screenMargin,
                    end = GlassTokens.screenMargin,
                    top = GlassTokens.widgetGap,
                    bottom = GlassTokens.widgetGap,
                ),
            )
        }
    }
}

@Composable
private fun HomePage(
    page: Int,
    items: List<WorkspaceItem>,
    apps: List<AppEntry>,
    icons: Map<String, Bitmap>,
    month: YearMonth,
    weather: WeatherUiState,
    wallpaperState: WidgetWallpaperState,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val builtins = items.filter { it.type == ItemType.WIDGET_BUILTIN }
    val appItems = items.filter { it.type == ItemType.APP }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = GlassTokens.screenMargin),
        verticalArrangement = Arrangement.spacedBy(GlassTokens.widgetGap),
    ) {
        // Top row: date + weather side by side.
        val dateItem = builtins.firstOrNull { it.builtinKind == DefaultLayout.KIND_DATE }
        val weatherItem = builtins.firstOrNull { it.builtinKind == DefaultLayout.KIND_WEATHER }
        if (dateItem != null || weatherItem != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GlassTokens.widgetGap),
            ) {
                if (dateItem != null) {
                    DateWidget(
                        item = dateItem,
                        date = viewModel.today,
                        wallpaper = wallpaperState,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (weatherItem != null) {
                    WeatherWidget(
                        item = weatherItem,
                        state = weather,
                        wallpaper = wallpaperState,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        builtins.firstOrNull { it.builtinKind == DefaultLayout.KIND_MONTH_CALENDAR }?.let { calendarItem ->
            MonthCalendarWidget(
                item = calendarItem,
                displayedMonth = month,
                today = viewModel.today,
                wallpaper = wallpaperState,
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth,
                onDateClick = viewModel::openCalendar,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (appItems.isNotEmpty()) {
            Text(
                text = "Aplikasi",
                color = GlassTokens.textSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
            AppGrid(appItems, apps, icons, viewModel, wallpaperState)
        }
    }
}

@Composable
private fun AppGrid(
    items: List<WorkspaceItem>,
    apps: List<AppEntry>,
    icons: Map<String, Bitmap>,
    viewModel: HomeViewModel,
    wallpaperState: WidgetWallpaperState,
) {
    // Phase 5 introduces real grid positioning; here we render a simple flowing row set.
    items.chunked(4).forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GlassTokens.widgetGap),
        ) {
            rowItems.forEach { item ->
                val icon = iconFor(item, apps, icons)
                AppIcon(
                    label = labelFor(item, apps),
                    bitmap = icon,
                    onClick = { viewModel.openDockItem(item) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun labelFor(item: WorkspaceItem, apps: List<AppEntry>): String =
    apps.firstOrNull { it.componentName == item.componentName }?.label ?: item.title ?: ""

private fun iconFor(item: WorkspaceItem, apps: List<AppEntry>, icons: Map<String, Bitmap>): ImageBitmap? {
    val entry = apps.firstOrNull { it.componentName == item.componentName } ?: return null
    return icons[entry.key]?.asImageBitmap()
}
