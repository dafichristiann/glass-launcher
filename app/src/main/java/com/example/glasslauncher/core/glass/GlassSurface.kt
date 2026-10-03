package com.example.glasslauncher.core.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * The single glass primitive (PRD section 6).
 *
 * Every card, tile, dock, search bar, sheet, toggle and slider track MUST go through this
 * composable. It draws, in order: a crop of the precomputed blurred wallpaper (Strategy A) or a
 * flat fill (Strategy C), a white fill, a gradient stroke, a top highlight, and a soft shadow.
 * No realtime blur is performed per element.
 *
 * @param blurredWallpaper the whole-screen blurred wallpaper (Strategy A source)
 * @param wallpaperScreenSize the screen size (px) the blurred wallpaper maps onto
 * @param parallaxOffsetX horizontal wallpaper shift in px (PRD section 8, optional)
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(GlassTokens.widgetRadius),
    blur: GlassBlur = GlassBlur.Medium,
    emphasis: GlassEmphasis = GlassEmphasis.Standard,
    edge: GlassEdge = GlassEdge.Medium,
    strategy: GlassStrategy = GlassStrategy.Precomputed,
    blurredWallpaper: ImageBitmap? = null,
    wallpaperScreenSize: IntSize = IntSize.Zero,
    parallaxOffsetX: Float = 0f,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    var posInRoot by remember { mutableStateOf(Offset.Zero) }

    val precomputed = strategy == GlassStrategy.Precomputed &&
        blurredWallpaper != null &&
        wallpaperScreenSize.width > 0

    Box(
        modifier = modifier
            .onGloballyPositioned { posInRoot = it.positionInRoot() }
            .clip(shape)
            .drawBehind {
                drawGlass(
                    shape = shape,
                    edge = edge,
                    emphasis = emphasis,
                    precomputed = precomputed,
                    blurredWallpaper = blurredWallpaper,
                    wallpaperScreenSize = wallpaperScreenSize,
                    positionX = posInRoot.x,
                    positionY = posInRoot.y,
                    parallaxOffsetX = parallaxOffsetX,
                    density = density.density,
                )
            },
        content = content,
    )
}

private fun DrawScope.drawGlass(
    shape: Shape,
    edge: GlassEdge,
    emphasis: GlassEmphasis,
    precomputed: Boolean,
    blurredWallpaper: ImageBitmap?,
    wallpaperScreenSize: IntSize,
    positionX: Float,
    positionY: Float,
    parallaxOffsetX: Float,
    density: Float,
) {
    val w = size.width
    val h = size.height
    val path = shape.toPath(w, h, this)

    // --- 1. Blurred wallpaper crop (Strategy A) ---
    if (precomputed && blurredWallpaper != null) {
        // Map the element's screen rect into the blurred bitmap's own pixel space.
        val scaleX = blurredWallpaper.width.toFloat() / wallpaperScreenSize.width.toFloat()
        val scaleY = blurredWallpaper.height.toFloat() / wallpaperScreenSize.height.toFloat()
        val srcX = ((positionX - parallaxOffsetX) * scaleX).roundToInt()
        val srcY = (positionY * scaleY).roundToInt()
        val srcW = maxOf(1, (w * scaleX).roundToInt())
        val srcH = maxOf(1, (h * scaleY).roundToInt())

        // Clip the crop rect to the bitmap bounds.
        val clampedX = srcX.coerceIn(0, maxOf(0, blurredWallpaper.width - 1))
        val clampedY = srcY.coerceIn(0, maxOf(0, blurredWallpaper.height - 1))
        val clampedW = minOf(srcW, blurredWallpaper.width - clampedX)
        val clampedH = minOf(srcH, blurredWallpaper.height - clampedY)

        if (clampedW > 0 && clampedH > 0) {
            drawImage(
                image = blurredWallpaper,
                srcOffset = IntOffset(clampedX, clampedY),
                srcSize = IntSize(clampedW, clampedH),
                dstOffset = IntOffset(0, 0),
                dstSize = IntSize(w.toInt(), h.toInt()),
            )
        }
    }

    // --- 2. White fill ---
    val fill = if (precomputed) GlassTokens.glassFillStrong else GlassTokens.flatGlassFill
    drawPath(path, color = fill)

    if (emphasis == GlassEmphasis.Strong) {
        drawPath(path, color = GlassTokens.overlayReadable)
    }

    // --- 3. Gradient stroke (top-left bright -> bottom-right faint) ---
    val strokeBrush = Brush.linearGradient(
        colors = listOf(GlassTokens.glassStrokeStart, GlassTokens.glassStrokeEnd),
        start = Offset.Zero,
        end = Offset(w, h),
    )
    drawPath(path, brush = strokeBrush, style = Stroke(width = edge.strokeWidth.toPx(density)))

    // --- 4. Top highlight line ---
    val highlightWidth = 1.dp.toPx(density)
    val cornerPx = GlassTokens.widgetRadius.toPx(density)
    val radius = minOf(cornerPx, w / 2f, h / 2f)
    drawLine(
        color = GlassTokens.glassHighlight.copy(alpha = edge.highlightAlpha),
        start = Offset(radius, highlightWidth / 2f),
        end = Offset(w - radius, highlightWidth / 2f),
        strokeWidth = highlightWidth,
    )

    // --- 5. Soft bottom shadow ---
    val shadowOffset = 4.dp.toPx(density)
    val shadowPath = shape.toPath(w, h, this).apply {
        translate(Offset(0f, shadowOffset))
    }
    drawPath(
        path = shadowPath,
        color = GlassTokens.shadow,
        style = Stroke(width = 1.dp.toPx(density)),
    )
}

private fun androidx.compose.ui.unit.Dp.toPx(density: Float): Float = value * density

private fun Shape.toPath(w: Float, h: Float, drawScope: DrawScope): Path {
    val outline = createOutline(Size(w, h), drawScope.layoutDirection, drawScope)
    val path = Path()
    if (outline is Outline.Rounded || outline is Outline.Generic) {
        path.addOutline(outline)
    } else {
        // Rectangular / default: fall back to a rect path.
        path.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
    }
    return path
}
