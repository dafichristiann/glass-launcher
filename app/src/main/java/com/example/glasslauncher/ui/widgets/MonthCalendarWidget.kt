package com.example.glasslauncher.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glasslauncher.core.glass.GlassSurface
import com.example.glasslauncher.core.glass.GlassTokens
import com.example.glasslauncher.data.workspace.WorkspaceItem
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Month calendar widget (PRD section 9.1).
 *
 * Header (prev, "Month Year", next), weekday initials following the locale's first day of week,
 * a 7-column grid with today circled. All months are computed; nothing is hardcoded.
 */
@Composable
fun MonthCalendarWidget(
    item: WorkspaceItem,
    displayedMonth: YearMonth,
    today: LocalDate,
    wallpaper: WidgetWallpaperState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        modifier = modifier,
        blurredWallpaper = wallpaper.image,
        wallpaperScreenSize = wallpaper.screenSize,
        strategy = wallpaper.strategy,
    ) {
        Column(modifier = Modifier.padding(GlassTokens.cardPadding)) {
            CalendarHeader(displayedMonth, onPrevious, onNext)
            WeekdayRow(displayedMonth)
            CalendarGrid(displayedMonth, today, onDateClick)
        }
    }
}

@Composable
private fun CalendarHeader(month: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.ChevronLeft,
            contentDescription = "Bulan sebelumnya",
            tint = GlassTokens.cardValue,
            modifier = Modifier.size(24.dp).clickable(onClick = onPrevious),
        )
        Text(
            text = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                .replaceFirstChar { it.titlecase(Locale.getDefault()) } + " ${month.year}",
            color = GlassTokens.cardValue,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = "Bulan berikutnya",
            tint = GlassTokens.cardValue,
            modifier = Modifier.size(24.dp).clickable(onClick = onNext),
        )
    }
}

@Composable
private fun WeekdayRow(month: YearMonth) {
    val locale = Locale.getDefault()
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    Row(modifier = Modifier.fillMaxWidth()) {
        repeat(7) { i ->
            val dayOfWeek = firstDay.plus(i.toLong())
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
                color = GlassTokens.cardHeading,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
) {
    val locale = Locale.getDefault()
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    val firstOfMonth = month.atDay(1)
    val leadingBlanks = ((firstOfMonth.dayOfWeek.value - firstDay.value) + 7) % 7
    val daysInMonth = month.lengthOfMonth()
    val totalCells = leadingBlanks + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.fillMaxWidth()) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - leadingBlanks + 1
                    val cellModifier = Modifier.weight(1f)
                    if (dayNumber in 1..daysInMonth) {
                        val date = month.atDay(dayNumber)
                        DayCell(date, date == today, cellModifier, onDateClick)
                    } else {
                        Box(modifier = cellModifier)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isToday: Boolean,
    modifier: Modifier,
    onDateClick: (LocalDate) -> Unit,
) {
    Box(
        modifier = modifier.size(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(
                    color = if (isToday) GlassTokens.cardValue else androidx.compose.ui.graphics.Color.Transparent,
                    shape = CircleShape,
                )
                .clickable { onDateClick(date) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = if (isToday) androidx.compose.ui.graphics.Color.White else GlassTokens.cardValue,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

