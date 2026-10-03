package com.example.glasslauncher.core.icons

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import androidx.annotation.WorkerThread
import com.example.glasslauncher.core.glass.GlassTokens

/**
 * Renders one app icon into a single finished glass tile bitmap (PRD section 7).
 *
 * The output is a ready-to-draw [Bitmap]; the UI only draws it and never computes glass during
 * scroll. Runs on Dispatchers.Default; callers must not call this on the main thread.
 */
class GlassIconRenderer {

    /**
     * @param icon the app's real drawable (default icon — no icon pack)
     * @param density device density, to size the output in pixels
     */
    @WorkerThread
    fun render(icon: Drawable?, request: GlassIconRequest, density: Float): Bitmap {
        val px = (request.tileSizeDp * density).toInt().coerceAtLeast(1)
        val output = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // --- Glass tile background ---
        val tilePath = tilePath(request.tileShape, px, density)
        val fillAlpha = (request.glassFillAlphaPercent.coerceIn(8, 20) / 100f * 255).toInt()
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(fillAlpha + 40, 255, 255, 255)
        }
        canvas.drawPath(tilePath, fillPaint)

        // Gradient stroke + top highlight via simple two-tone outline.
        val strokeWidth = (request.edge.strokeWidth.value * density)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            color = Color.argb(0x99, 255, 255, 255)
        }
        canvas.drawPath(tilePath, strokePaint)

        // --- App foreground on top ---
        if (icon != null) {
            val foreground = extractForeground(icon)
            val glyphTarget = (px * 0.70f).toInt()
            val glyph = foreground.toBitmap(
                width = glyphTarget,
                height = glyphTarget,
                config = Bitmap.Config.ARGB_8888,
            )
            if (request.style == IconStyle.Monochrome) {
                applyMonochrome(glyph, GlassTokens.textPrimary.toArgb())
            }
            if (request.saturationPercent > 0) {
                // Kelembutan warna handled by desaturating in a later tuning pass; the
                // pipeline keeps the original color here so it stays a single bitmap.
            }
            val left = (px - glyphTarget) / 2f
            val top = (px - glyphTarget) / 2f
            canvas.drawBitmap(glyph, left, top, Paint(Paint.FILTER_BITMAP_FLAG))
        }

        return output
    }

    private fun extractForeground(icon: Drawable): Drawable = when (icon) {
        is AdaptiveIconDrawable -> icon.foreground ?: icon
        else -> icon
    }

    private fun applyMonochrome(bitmap: Bitmap, colorArgb: Int) {
        val r = Color.red(colorArgb)
        val g = Color.green(colorArgb)
        val b = Color.blue(colorArgb)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (i in pixels.indices) {
            val lum = (Color.red(pixels[i]) * 0.299f +
                Color.green(pixels[i]) * 0.587f +
                Color.blue(pixels[i]) * 0.114f).toInt().coerceIn(0, 255)
            val alpha = pixels[i] ushr 24
            pixels[i] = Color.argb(alpha, (r * lum / 255), (g * lum / 255), (b * lum / 255))
        }
        bitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    }

    private fun tilePath(shape: TileShape, px: Int, density: Float): Path {
        val path = Path()
        when (shape) {
            TileShape.Circle -> path.addCircle(px / 2f, px / 2f, px / 2f, Path.Direction.CW)
            TileShape.RoundedSquare -> {
                val radius = 18f * density
                path.addRoundRect(RectF(0f, 0f, px.toFloat(), px.toFloat()), radius, radius, Path.Direction.CW)
            }
            TileShape.Squircle -> {
                val radius = px * 0.30f
                path.addRoundRect(RectF(0f, 0f, px.toFloat(), px.toFloat()), radius, radius, Path.Direction.CW)
            }
        }
        return path
    }
}

private fun androidx.compose.ui.graphics.Color.toArgb(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt(),
)
