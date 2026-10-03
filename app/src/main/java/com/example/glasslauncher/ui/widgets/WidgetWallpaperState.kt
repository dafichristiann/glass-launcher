package com.example.glasslauncher.ui.widgets

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import com.example.glasslauncher.core.glass.GlassStrategy

/**
 * Shared, stable state passed to every widget/dock/tile so each [GlassSurface] draws the right
 * crop of the precomputed blurred wallpaper without recomposing on every frame.
 */
@Immutable
data class WidgetWallpaperState(
    val image: ImageBitmap?,
    val screenSize: IntSize,
    val strategy: GlassStrategy,
) {
    companion object {
        val Empty = WidgetWallpaperState(null, IntSize.Zero, GlassStrategy.Flat)
    }
}
