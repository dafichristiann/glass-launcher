package com.example.glasslauncher.core.glass

import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/** Glass blur intensity (PRD section 6). */
enum class GlassBlur { Light, Medium, Heavy }

/** Glass emphasis: Strong adds the readability overlay (drawer/settings). */
enum class GlassEmphasis { Standard, Strong }

/** Render strategy (PRD section 6). */
enum class GlassStrategy { Precomputed, Live, Flat }

/**
 * A single frosted-glass layer descriptor. The wallpaper source is a precomputed blurred bitmap
 * (Strategy A); the layer just draws the correct crop plus fill/stroke/highlight/shadow.
 */
data class GlassLayer(
    val shape: Shape,
    val blur: GlassBlur = GlassBlur.Medium,
    val emphasis: GlassEmphasis = GlassEmphasis.Standard,
    val edge: GlassEdge = GlassEdge.Medium,
    val strategy: GlassStrategy = GlassStrategy.Precomputed,
    val cornerRadius: Dp = GlassTokens.widgetRadius,
)
