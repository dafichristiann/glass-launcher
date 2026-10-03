package com.example.glasslauncher.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.glasslauncher.core.glass.GlassSurface
import com.example.glasslauncher.core.glass.GlassTokens
import com.example.glasslauncher.data.workspace.WorkspaceItem

/**
 * Weather widget (PRD section 9.1 / 11).
 *
 * The Open-Meteo data source is wired in Phase 3; until then this renders the design's structure
 * with an honest "unavailable" state rather than a hardcoded mockup temperature.
 */
@Composable
fun WeatherWidget(
    item: WorkspaceItem,
    state: WeatherUiState,
    wallpaper: WidgetWallpaperState,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        modifier = modifier,
        blurredWallpaper = wallpaper.image,
        wallpaperScreenSize = wallpaper.screenSize,
        strategy = wallpaper.strategy,
    ) {
        Column(modifier = Modifier.padding(GlassTokens.cardPadding)) {
            Text(text = state.city, color = GlassTokens.cardHeading, fontSize = 13.sp)
            Text(
                text = state.temperatureLabel,
                color = GlassTokens.cardValue,
                fontSize = GlassTokens.widgetBigNumberSize,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = state.conditionLabel, color = GlassTokens.cardHeading, fontSize = 13.sp)
        }
    }
}

/** Phase-3 placeholder state; phase 0-2 shows an unavailable condition. */
data class WeatherUiState(
    val city: String,
    val temperatureLabel: String,
    val conditionLabel: String,
) {
    companion object {
        fun unavailable(city: String) = WeatherUiState(
            city = city,
            temperatureLabel = "--°",
            conditionLabel = "Cuaca tidak tersedia",
        )
    }
}
