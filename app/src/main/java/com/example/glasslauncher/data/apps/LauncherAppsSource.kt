package com.example.glasslauncher.data.apps

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import android.os.UserManager

/**
 * Thin wrapper over [LauncherApps] so it can be faked in tests (LauncherApps is final/system).
 *
 * User profiles are currently identified by the profile's `hashCode`; multi-profile support
 * (work profile dock/launch targets) is a later-phase concern.
 */
interface LauncherAppsSource {
    fun queryLaunchableApps(): List<AppEntry>
    fun registerCallback(callback: LauncherApps.Callback)
    fun unregisterCallback(callback: LauncherApps.Callback)
    fun launchApp(componentName: String, userSerial: Long)
    fun getUserForSerial(serial: Long): UserHandle?
}

class AndroidLauncherAppsSource(private val context: Context) : LauncherAppsSource {

    private val launcherApps: LauncherApps? =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps

    private val userManager: UserManager? =
        context.getSystemService(Context.USER_SERVICE) as? UserManager

    override fun queryLaunchableApps(): List<AppEntry> {
        val la = launcherApps ?: return emptyList()
        val density = context.resources.displayMetrics.densityDpi
        val users = runCatching { userManager?.userProfiles }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?: listOf(Process.myUserHandle())

        val result = ArrayList<AppEntry>()
        for (user in users) {
            val activities = runCatching { la.getActivityList(null, user) }.getOrDefault(emptyList())
            for (activity in activities) {
                if (!activity.applicationInfo.enabled) continue
                result.add(
                    AppEntry(
                        packageName = activity.applicationInfo.packageName,
                        componentName = activity.componentName.flattenToString(),
                        label = activity.label?.toString() ?: activity.applicationInfo.packageName,
                        userSerial = user.stableId(),
                        icon = runCatching { activity.getIcon(density) }.getOrNull(),
                    ),
                )
            }
        }
        return result
    }

    override fun registerCallback(callback: LauncherApps.Callback) {
        launcherApps?.registerCallback(callback)
    }

    override fun unregisterCallback(callback: LauncherApps.Callback) {
        launcherApps?.unregisterCallback(callback)
    }

    override fun launchApp(componentName: String, userSerial: Long) {
        val la = launcherApps ?: return
        val user = getUserForSerial(userSerial) ?: return
        val component = ComponentName.unflattenFromString(componentName) ?: return
        val sourceBounds: android.graphics.Rect? = null
        val options: android.os.Bundle? = null
        runCatching { la.startMainActivity(component, user, sourceBounds, options) }
    }

    override fun getUserForSerial(serial: Long): UserHandle? {
        val profiles = runCatching { userManager?.userProfiles }.getOrNull() ?: return null
        return profiles.firstOrNull { it.stableId() == serial }
    }
}

/** Stable per-profile identifier: the handle's own hash is consistent within a process. */
private fun UserHandle.stableId(): Long = hashCode().toLong()
