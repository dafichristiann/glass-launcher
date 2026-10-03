package com.example.glasslauncher.ui.icon

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glasslauncher.core.glass.GlassTokens

/**
 * Renders a finished glass icon bitmap (PRD section 7). The bitmap already contains the tile and
 * the app foreground; this composable only displays it and the optional label. It never computes
 * glass during scroll.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(
    label: String?,
    bitmap: ImageBitmap?,
    modifier: Modifier = Modifier,
    tileSizeDp: Int = 60,
    showLabel: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val iconModifier = Modifier
            .size(tileSizeDp.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = label,
                modifier = iconModifier,
            )
        } else {
            Box(
                modifier = iconModifier.background(
                    color = GlassTokens.glassFillStrong,
                    shape = RoundedCornerShape(18.dp),
                ),
            )
        }

        if (showLabel && label != null) {
            Text(
                text = label,
                color = GlassTokens.textPrimary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}
