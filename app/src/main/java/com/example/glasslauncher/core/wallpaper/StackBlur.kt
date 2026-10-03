package com.example.glasslauncher.core.wallpaper

import android.graphics.Bitmap

/**
 * Pure-Kotlin StackBlur, used only for Android < 12 (Strategy C fallback path).
 * Runs off the main thread; never called per frame.
 */
internal object StackBlur {

    fun blur(source: Bitmap, radius: Int): Bitmap {
        if (radius < 1) return source.copy(Bitmap.Config.ARGB_8888, false)

        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val wm = width - 1
        val hm = height - 1
        val div = radius + radius + 1
        val r = IntArray(width * height)
        val g = IntArray(width * height)
        val b = IntArray(width * height)
        val vmin = IntArray(maxOf(width, height))

        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        for (i in 0 until 256 * divsum) dv[i] = i / divsum

        var yw = 0
        var yi = 0

        val stack = Array(div) { IntArray(3) }
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        for (y in 0 until height) {
            rsum = 0; gsum = 0; bsum = 0
            routsum = 0; goutsum = 0; boutsum = 0
            rinsum = 0; ginsum = 0; binsum = 0
            for (i in -radius..radius) {
                val p = pixels[yi + minOf(wm, maxOf(i, 0))]
                val sir = stack[i + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff
                val rbs = radius + 1 - kotlin.math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]; ginsum += sir[1]; binsum += sir[2]
                } else {
                    routsum += sir[0]; goutsum += sir[1]; boutsum += sir[2]
                }
            }
            var stackpointer = radius
            for (x in 0 until width) {
                r[yi] = dv[rsum]; g[yi] = dv[gsum]; b[yi] = dv[bsum]
                rsum -= routsum; gsum -= goutsum; bsum -= boutsum
                val stackstart = stackpointer - radius + div
                val sir = stack[stackstart % div]
                routsum -= sir[0]; goutsum -= sir[1]; boutsum -= sir[2]
                if (y == 0) vmin[x] = minOf(x + radius + 1, wm)
                val p = pixels[yw + vmin[x]]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff
                rinsum += sir[0]; ginsum += sir[1]; binsum += sir[2]
                rsum += rinsum; gsum += ginsum; bsum += binsum
                stackpointer = (stackpointer + 1) % div
                val sir2 = stack[stackpointer % div]
                routsum += sir2[0]; goutsum += sir2[1]; boutsum += sir2[2]
                rinsum -= sir2[0]; ginsum -= sir2[1]; binsum -= sir2[2]
                yi++
            }
            yw += width
        }

        for (x in 0 until width) {
            rsum = 0; gsum = 0; bsum = 0
            routsum = 0; goutsum = 0; boutsum = 0
            rinsum = 0; ginsum = 0; binsum = 0
            var yp = -radius * width
            for (i in -radius..radius) {
                val yiIndex = maxOf(0, yp) + x
                val sir = stack[i + radius]
                sir[0] = r[yiIndex]; sir[1] = g[yiIndex]; sir[2] = b[yiIndex]
                val rbs = radius + 1 - kotlin.math.abs(i)
                rsum += sir[0] * rbs; gsum += sir[1] * rbs; bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]; ginsum += sir[1]; binsum += sir[2]
                } else {
                    routsum += sir[0]; goutsum += sir[1]; boutsum += sir[2]
                }
                if (i < hm) yp += width
            }
            yi = x
            var stackpointer = radius
            for (y in 0 until height) {
                pixels[yi] = (0xff shl 24) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                rsum -= routsum; gsum -= goutsum; bsum -= boutsum
                val stackstart = stackpointer - radius + div
                val sir = stack[stackstart % div]
                routsum -= sir[0]; goutsum -= sir[1]; boutsum -= sir[2]
                if (x == 0) vmin[y] = minOf(y + radius + 1, hm) * width
                val p = x + vmin[y]
                sir[0] = r[p]; sir[1] = g[p]; sir[2] = b[p]
                rinsum += sir[0]; ginsum += sir[1]; binsum += sir[2]
                rsum += rinsum; gsum += ginsum; bsum += binsum
                stackpointer = (stackpointer + 1) % div
                val sir2 = stack[stackpointer]
                routsum += sir2[0]; goutsum += sir2[1]; boutsum += sir2[2]
                rinsum -= sir2[0]; ginsum -= sir2[1]; binsum -= sir2[2]
                yi += width
            }
        }

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }
}
