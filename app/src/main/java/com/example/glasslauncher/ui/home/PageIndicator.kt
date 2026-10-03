package com.example.glasslauncher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.glasslauncher.R
import com.example.glasslauncher.core.glass.GlassTokens

/** Page indicator (PRD section 9.1): active dot darker, inactive muted. */
@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    if (pageCount <= 1) return

    val description = stringResource(R.string.page_indicator_desc, currentPage + 1, pageCount)

    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (active) 7.dp else 6.dp)
                    .background(
                        color = if (active) GlassTokens.pageDotActive else GlassTokens.pageDotInactive,
                        shape = CircleShape,
                    ),
            )
        }
    }
}
