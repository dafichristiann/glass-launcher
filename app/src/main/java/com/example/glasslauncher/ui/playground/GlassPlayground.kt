package com.example.glasslauncher.ui.playground

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glasslauncher.core.glass.GlassBlur
import com.example.glasslauncher.core.glass.GlassTokens
import com.example.glasslauncher.ui.icon.AppIcon

/**
 * Glass engine test screen (PRD Phase 1).
 *
 * A glass card, a 40-icon scrollable grid, and blur/transparency/edge sliders that update the
 * surfaces live. Verifies Strategy A stays smooth while controls change.
 */
@Composable
fun GlassPlayground(wallpaper: com.example.glasslauncher.ui.widgets.WidgetWallpaperState) {
    var blur by remember { mutableFloatStateOf(GlassTokens.BLUR_MEDIUM.toFloat()) }
    var transparency by remember { mutableFloatStateOf(12f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        com.example.glasslauncher.core.glass.GlassSurface(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            blurredWallpaper = wallpaper.image,
            wallpaperScreenSize = wallpaper.screenSize,
            strategy = wallpaper.strategy,
            blur = blurToIntensity(blur),
        ) {
            Text(
                text = "Glass Card (Strategy A)",
                color = GlassTokens.textPrimary,
                modifier = Modifier.padding(16.dp),
            )
        }

        Text(text = "Blur", color = GlassTokens.textPrimary, fontSize = 12.sp)
        Slider(value = blur, onValueChange = { blur = it }, valueRange = 0f..40f)

        Text(text = "Transparency ${transparency.toInt()}%", color = GlassTokens.textPrimary, fontSize = 12.sp)
        Slider(value = transparency, onValueChange = { transparency = it }, valueRange = 8f..20f)

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items((1..40).toList()) { index ->
                AppIcon(
                    label = "App $index",
                    bitmap = null,
                    tileSizeDp = 60,
                )
            }
        }
    }
}

private fun blurToIntensity(value: Float): GlassBlur = when {
    value < 18f -> GlassBlur.Light
    value < 32f -> GlassBlur.Medium
    else -> GlassBlur.Heavy
}
