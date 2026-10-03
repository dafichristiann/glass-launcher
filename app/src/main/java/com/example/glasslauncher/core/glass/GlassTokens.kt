package com.example.glasslauncher.core.glass

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Clear Glass design tokens.
 *
 * Values are taken from the canonical design file `untitled.pen` for the glass screens.
 * The obsolete "Minimalist Launcher" palette (primary/ink) is intentionally NOT used here.
 *
 * PRD section 4 is the written reference; where the .pen differs, the .pen value wins and the
 * difference is documented in the phase report.
 */
object GlassTokens {

    // --- Text ---
    /** PRD `textPrimary` #1F2F4A. Matches .pen icon/dock glyph fill. */
    val textPrimary = Color(0xFF1F2F4A)
    /** PRD `textSecondary` #5B6B85. */
    val textSecondary = Color(0xFF5B6B85)
    /** PRD `accent` #3F5A85. */
    val accent = Color(0xFF3F5A85)
    /** PRD `accentSoft` #A9C4E6. */
    val accentSoft = Color(0xFFA9C4E6)

    /**
     * Date/weather/calendar card heading colors as drawn in .pen (Home Page 1):
     * weekday/month use #3A567A, the large number uses #16304F.
     */
    val cardHeading = Color(0xFF3A567A)
    val cardValue = Color(0xFF16304F)

    // --- Glass surfaces ---
    /** PRD `glassFill` = white 12%; .pen `glass-fill` = #FFFFFF1F (~12%). */
    val glassFill = Color(0x1FFFFFFF)
    /** .pen `glass-fill-strong` = #FFFFFF33 (widget cards / dock). */
    val glassFillStrong = Color(0x33FFFFFF)
    /** .pen `glass-fill-strong` for the dock reference. */
    val dockFill = Color(0x33FFFFFF)
    /** PRD `glassStrokeStart` white 60% (top-left). .pen `stroke-top` #FFFFFF99. */
    val glassStrokeStart = Color(0x99FFFFFF)
    /** PRD `glassStrokeEnd` white 10% (bottom-right). .pen `stroke-bot` #FFFFFF1A. */
    val glassStrokeEnd = Color(0x1AFFFFFF)
    /** PRD `glassHighlight` white 35%. */
    val glassHighlight = Color(0x59FFFFFF)
    /** PRD `hairline` white 25%. .pen `hairline` #FFFFFF40. */
    val hairline = Color(0x40FFFFFF)
    /** PRD `shadow` #3F5A85 @ 12%. .pen `shadow` #2E4A6E1F (~12%). */
    val shadow = Color(0x1F2E4A6E)
    /** PRD `badge` soft red. */
    val badge = Color(0xFFE57373)
    /** PRD `overlayReadable` white 20%. */
    val overlayReadable = Color(0x33FFFFFF)

    /** Flat-glass fallback (Strategy C): white 18% fill, no blur. */
    val flatGlassFill = Color(0x2EFFFFFF)

    // --- Page indicator ---
    val pageDotActive = Color(0xFF16304F)
    val pageDotInactive = Color(0x6616304F)

    // --- Radii (PRD section 4) ---
    val iconRadius = 18.dp
    val widgetRadius = 28.dp
    val dockRadius = 32.dp
    val sheetRadius = 32.dp
    val notificationRadius = 24.dp
    val quickTileRadius = 22.dp
    val settingsIconTileRadius = 10.dp
    val pillRadius = 999.dp

    // --- Spacing (PRD section 4, base 4dp) ---
    val screenMargin = 16.dp
    val widgetGap = 12.dp
    val cardPadding = 16.dp
    val settingRowHeight = 56.dp
    val hairlineInset = 16.dp

    // --- Typography (PRD section 4) ---
    val titleSize = 28.sp
    val sectionHeaderSize = 13.sp
    val bodySize = 15.sp
    val iconLabelSize = 12.sp
    val captionSize = 12.sp
    val widgetBigNumberSize = 40.sp

    // --- Glass edge variants (PRD section 4) ---
    val edgeThinStroke = 0.75.dp
    val edgeMediumStroke = 1.dp
    val edgeThickStroke = 1.5.dp

    // --- Blur radii (PRD section 6) ---
    const val BLUR_LIGHT = 12
    const val BLUR_MEDIUM = 24
    const val BLUR_HEAVY = 40
}

/**
 * Glass edge thickness presets, matching PRD section 4 "Tepi kaca".
 */
enum class GlassEdge(val strokeWidth: androidx.compose.ui.unit.Dp, val highlightAlpha: Float) {
    Thin(GlassTokens.edgeThinStroke, 0.20f),
    Medium(GlassTokens.edgeMediumStroke, 0.35f),
    Thick(GlassTokens.edgeThickStroke, 0.50f),
}
