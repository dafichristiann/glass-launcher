package com.example.glasslauncher.data.apps

import android.graphics.drawable.Drawable

/** One launchable app (PRD section 5 data flow). */
data class AppEntry(
    val packageName: String,
    val componentName: String,
    val label: String,
    val userSerial: Long,
    val icon: Drawable?,
) {
    /** Stable key for layout persistence and list keys. */
    val key: String get() = "$userSerial/$componentName"
}
