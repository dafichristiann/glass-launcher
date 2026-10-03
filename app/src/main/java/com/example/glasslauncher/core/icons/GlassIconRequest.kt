package com.example.glasslauncher.core.icons

import com.example.glasslauncher.core.glass.GlassBlur
import com.example.glasslauncher.core.glass.GlassEdge
import com.example.glasslauncher.core.glass.GlassTokens

/** Complete set of inputs that affect a rendered glass icon bitmap (PRD section 7, cache key). */
data class GlassIconRequest(
    val packageName: String,
    val componentName: String,
    val userSerial: Long,
    val style: IconStyle = IconStyle.Original,
    val tileShape: TileShape = TileShape.RoundedSquare,
    val tileSizeDp: Int = 60,
    val glassFillAlphaPercent: Int = 12,
    val edge: GlassEdge = GlassEdge.Medium,
    val blur: GlassBlur = GlassBlur.Medium,
    val saturationPercent: Int = 10,
)

/** Builds the cache key described in PRD section 7. */
fun GlassIconRequest.cacheKey(): String = buildString {
    append(packageName).append('|')
    append(componentName).append('|')
    append(userSerial).append('|')
    append(style).append('|')
    append(tileShape).append('|')
    append(tileSizeDp).append('|')
    append(glassFillAlphaPercent).append('|')
    append(edge).append('|')
    append(blur).append('|')
    append(saturationPercent)
}
