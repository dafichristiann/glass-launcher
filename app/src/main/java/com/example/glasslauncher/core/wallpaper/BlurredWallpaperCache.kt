package com.example.glasslauncher.core.wallpaper

import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import com.example.glasslauncher.core.glass.GlassTokens

/**
 * Strategy A: precomputed frosted glass (PRD section 6).
 *
 * The launcher wallpaper is static, so "glass" is just a blurred crop of the wallpaper. We build
 * three blurred downscaled bitmaps once (light/medium/heavy), cached in memory, and each glass
 * surface samples the region behind itself by screen coordinate. Zero realtime blur per element.
 */
class BlurredWallpaperCache(
    private val source: Bitmap,
    private val downscale: Int = 4,
) {
    private val light by lazy { buildBlur(GlassTokens.BLUR_LIGHT) }
    private val medium by lazy { buildBlur(GlassTokens.BLUR_MEDIUM) }
    private val heavy by lazy { buildBlur(GlassTokens.BLUR_HEAVY) }

    /** Blurred wallpaper by intensity, at 1/[downscale] resolution. */
    @WorkerThread
    fun blurred(intensity: BlurIntensity): Bitmap = when (intensity) {
        BlurIntensity.Light -> light
        BlurIntensity.Medium -> medium
        BlurIntensity.Heavy -> heavy
    }

    @WorkerThread
    private fun buildBlur(radius: Int): Bitmap {
        val small = Bitmap.createScaledBitmap(
            source,
            (source.width / downscale).coerceAtLeast(1),
            (source.height / downscale).coerceAtLeast(1),
            true,
        )
        // StackBlur runs once per wallpaper, off the frame loop, on every API level. A
        // RenderEffect (API 31+) path can be added later as a quality upgrade; correctness and
        // smoothness matter more than which blur kernel produced the cached bitmap.
        return StackBlur.blur(small, radius)
    }

    enum class BlurIntensity { Light, Medium, Heavy }
}
