package com.example.glasslauncher.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glasslauncher.core.glass.GlassSurface
import com.example.glasslauncher.core.glass.GlassTokens
import com.example.glasslauncher.data.workspace.WorkspaceItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Date widget (PRD section 9.1). Day name, large day number, month name — all from the system
 * clock/locale. No hardcoded mockup values.
 */
@Composable
fun DateWidget(
    item: WorkspaceItem,
    date: LocalDate,
    wallpaper: WidgetWallpaperState,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.getDefault()
    val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    val month = date.month.getDisplayName(TextStyle.FULL, locale)

    GlassSurface(
        modifier = modifier,
        blurredWallpaper = wallpaper.image,
        wallpaperScreenSize = wallpaper.screenSize,
        strategy = wallpaper.strategy,
    ) {
        Column(modifier = Modifier.padding(GlassTokens.cardPadding)) {
            Text(text = weekday, color = GlassTokens.cardHeading, fontSize = 13.sp)
            Text(
                text = date.dayOfMonth.toString(),
                color = GlassTokens.cardValue,
                fontSize = GlassTokens.widgetBigNumberSize,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = month, color = GlassTokens.cardHeading, fontSize = 13.sp)
        }
    }
}

/** Format helper kept close to the widget so formatting stays in one place. */
internal fun LocalDate.monthYearLabel(locale: Locale = Locale.getDefault()): String {
    val formatter = DateTimeFormatter.ofPattern("LLLL yyyy", locale)
    return format(formatter).replaceFirstChar { it.titlecase(locale) }
}
