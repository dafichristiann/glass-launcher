package com.example.glasslauncher.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.example.glasslauncher.core.glass.GlassSurface
import com.example.glasslauncher.core.glass.GlassTokens
import com.example.glasslauncher.ui.icon.AppIcon
import com.example.glasslauncher.ui.widgets.WidgetWallpaperState

/** One dock slot's resolved content. */
data class DockSlot(
    val label: String,
    val icon: ImageBitmap?,
    val onClick: () -> Unit,
)

/**
 * Dock (PRD section 9.1). Glass rounded pill, 4 slots by default, all through [GlassSurface].
 */
@Composable
fun Dock(
    slots: List<DockSlot>,
    wallpaper: WidgetWallpaperState,
    modifier: Modifier = Modifier,
) {
    if (slots.isEmpty()) return

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GlassTokens.dockRadius),
        blurredWallpaper = wallpaper.image,
        wallpaperScreenSize = wallpaper.screenSize,
        strategy = wallpaper.strategy,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            slots.forEach { slot ->
                AppIcon(
                    label = null,
                    bitmap = slot.icon,
                    tileSizeDp = 54,
                    showLabel = false,
                    onClick = slot.onClick,
                )
            }
        }
    }
}
