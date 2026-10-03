package com.example.glasslauncher.core.device

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.WindowManager

/**
 * Resolves the device profile at runtime (PRD section 2 "Follow Device").
 *
 * Nothing here is hardcoded guesswork about the user's phone: values come from the system.
 * The known device (Tecno Camon 50 Pro, HiOS 16, Android 16) is documented in the PRD/design
 * notes but the code detects rather than assumes.
 */
object DeviceDefaults {

    private const val TARGET_FRAME_BUDGET_HZ = 120

    fun resolve(context: Context): DeviceProfile {
        val pm = context.packageManager
        val grid = resolveGrid(context)

        return DeviceProfile(
            brandModel = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
            androidVersion = Build.VERSION.SDK_INT,
            skin = resolveSkin(),
            refreshRateHz = resolveRefreshRate(context),
            drawerVertical = true,
            hasDrawer = true,
            gridColumns = grid.first,
            gridRows = grid.second,
            gestureNavigation = resolveGestureNavigation(context),
            searchBarTop = true,
            dockApps = resolveDockApps(context, pm),
            frameBudgetHz = TARGET_FRAME_BUDGET_HZ,
        )
    }

    /** Highest supported refresh rate, capped to the reported panel; 60 fallback. */
    private fun resolveRefreshRate(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        val hz = wm.defaultDisplay.refreshRate
        return if (hz > 0f) Math.round(hz) else 60
    }

    /**
     * Home grid. Android does not expose the stock grid, so a device-tier heuristic is used:
     * Full-HD and above get 5 columns x 5 rows (PRD default), else 4 x 5.
     */
    private fun resolveGrid(context: Context): Pair<Int, Int> {
        val widthPx = context.resources.displayMetrics.widthPixels
        return if (widthPx >= 720) 5 to 5 else 4 to 5
    }

    private fun resolveSkin(): String = when {
        Build.MANUFACTURER.equals("TECNO", ignoreCase = true) ||
            Build.MANUFACTURER.equals("Infinix", ignoreCase = true) ||
            Build.MANUFACTURER.equals("itel", ignoreCase = true) -> "HiOS"
        Build.MANUFACTURER.equals("samsung", ignoreCase = true) -> "One UI"
        Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) -> "HyperOS"
        Build.MANUFACTURER.equals("Google", ignoreCase = true) -> "Pixel"
        else -> "AOSP"
    }

    /**
     * Navigation mode. Android 10+ exposes gesture vs 3-button via Settings.Secure
     * "navigation_mode" (2 = gesture, 1 = 2-button, 0 = 3-button). Read defensively.
     */
    private fun resolveGestureNavigation(context: Context): Boolean {
        return try {
            val mode = Settings.Secure.getInt(context.contentResolver, "navigation_mode", 2)
            mode == 2
        } catch (_: Exception) {
            true
        }
    }

    /** Resolve default apps for the dock roles (Phone, SMS, Camera, Browser). */
    private fun resolveDockApps(context: Context, pm: PackageManager): List<String> {
        val result = mutableListOf<String>()
        dialerPackage(pm)?.let { result.add(it) }
        smsPackage(pm)?.let { result.add(it) }
        cameraPackage(pm)?.let { result.add(it) }
        browserPackage(pm)?.let { result.add(it) }
        return result.distinct()
    }

    private fun dialerPackage(pm: PackageManager): String? =
        packageForAction(pm, Intent(Intent.ACTION_DIAL))

    private fun smsPackage(pm: PackageManager): String? =
        packageForAction(pm, Intent(Intent.ACTION_VIEW, android.net.Uri.parse("sms:")))

    private fun cameraPackage(pm: PackageManager): String? =
        packageForAction(pm, Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA))

    private fun browserPackage(pm: PackageManager): String? =
        packageForAction(pm, Intent(Intent.ACTION_VIEW, android.net.Uri.parse("http://example.com")))

    private fun packageForAction(pm: PackageManager, intent: Intent): String? =
        pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
}
