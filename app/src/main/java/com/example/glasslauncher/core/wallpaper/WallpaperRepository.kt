package com.example.glasslauncher.core.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.toBitmap
import com.example.glasslauncher.R

/**
 * Provides the launcher wallpaper bitmap.
 *
 * PRD section 8, wallpaper mode 1 (default): the design wallpaper bundled at
 * `res/drawable-nodpi/wallpaper_misty_mountains.png`. The launcher draws its own wallpaper so
 * the glass engine (Strategy A) can read the pixels behind each element.
 */
class WallpaperRepository(private val context: Context) {

    private var cached: Bitmap? = null

    /** The default design wallpaper, decoded once and cached in memory. */
    fun designWallpaper(): Bitmap {
        cached?.let { return it }
        val bitmap = decodeScaled(R.drawable.wallpaper_misty_mountains)
            ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        cached = bitmap
        return bitmap
    }

    private fun decodeScaled(resId: Int): Bitmap? {
        val opts = BitmapFactory.Options().apply { inScaled = false }
        return try {
            BitmapFactory.decodeResource(context.resources, resId, opts)
        } catch (_: Exception) {
            null
        }
    }

    /** Decode an arbitrary drawable into a bitmap (used by tests / future wallpaper picker). */
    fun fromDrawableRes(resId: Int): Bitmap? =
        androidx.core.content.ContextCompat.getDrawable(context, resId)?.toBitmap()
}
