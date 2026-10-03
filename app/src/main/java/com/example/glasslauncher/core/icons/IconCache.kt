package com.example.glasslauncher.core.icons

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Rendered glass icon bitmaps, in memory (LruCache) and on disk (PRD section 7).
 *
 * Disk keeps a rendered icon across process restarts; memory avoids re-decoding during scroll.
 * All access happens off the main thread (see [GlassIconRenderer]).
 */
class IconCache(context: Context) {

    private val diskDir: File = File(context.cacheDir, "glass_icons").apply { mkdirs() }

    private val memory = object : LruCache<String, Bitmap>(memoryBudgetBytes()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun get(key: String): Bitmap? {
        memory.get(key)?.let { return it }
        val file = diskFile(key)
        if (!file.exists()) return null
        return runCatching {
            val bmp = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            if (bmp != null) memory.put(key, bmp)
            bmp
        }.getOrNull()
    }

    fun put(key: String, bitmap: Bitmap) {
        memory.put(key, bitmap)
        runCatching {
            FileOutputStream(diskFile(key)).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }

    fun clear() {
        memory.evictAll()
        diskDir.listFiles()?.forEach { it.delete() }
    }

    private fun diskFile(key: String): File = File(diskDir, sha1(key) + ".png")

    private fun sha1(value: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun memoryBudgetBytes(): Int {
        val maxHeap = Runtime.getRuntime().maxMemory()
        // Use at most 1/8 of the heap for icon bitmaps.
        return (maxHeap / 8).toInt().coerceAtLeast(4 * 1024 * 1024)
    }
}
