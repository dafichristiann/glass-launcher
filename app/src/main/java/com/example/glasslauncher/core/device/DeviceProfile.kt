package com.example.glasslauncher.core.device

/**
 * Immutable snapshot of the user's device profile (PRD section 2).
 *
 * Values that the PRD marks "Follow Device" are resolved at runtime; see [DeviceDefaults].
 * Defaults fall back to Pixel/AOSP when a value cannot be resolved (PRD section 18).
 */
data class DeviceProfile(
    val brandModel: String,
    val androidVersion: Int,
    val skin: String,
    val refreshRateHz: Int,
    /** Whether the stock drawer is a vertical scroll (true) or horizontal pager (false). */
    val drawerVertical: Boolean,
    val hasDrawer: Boolean,
    val gridColumns: Int,
    val gridRows: Int,
    val gestureNavigation: Boolean,
    val searchBarTop: Boolean,
    val dockApps: List<String>,
    /** Target frame budget. Kept at 120 even on 144Hz panels (PRD/user note). */
    val frameBudgetHz: Int,
)
