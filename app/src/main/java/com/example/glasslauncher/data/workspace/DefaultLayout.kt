package com.example.glasslauncher.data.workspace

import com.example.glasslauncher.core.device.DeviceProfile
import com.example.glasslauncher.data.apps.AppEntry

/**
 * Builds the first-run home layout (PRD section 9.1).
 *
 * Page 1 layout, matching the .pen reference:
 *  - row 0: Date widget (left) and Weather widget (right), each ~half width
 *  - rows 1-3: full-width month calendar
 *  - dock: resolved role apps (Phone, SMS, Camera, Browser) by default
 */
object DefaultLayout {

    const val KIND_DATE = "date"
    const val KIND_WEATHER = "weather"
    const val KIND_MONTH_CALENDAR = "month_calendar"

    fun build(profile: DeviceProfile, dockAppComponents: List<Pair<String, String>>): List<WorkspaceItem> =
        widgetsOnly(profile) + dockOnly(profile, dockAppComponents)

    /** The page-1 built-in widgets (date, weather, month calendar). */
    fun widgetsOnly(profile: DeviceProfile): List<WorkspaceItem> {
        val items = mutableListOf<WorkspaceItem>()
        items += WorkspaceItem(
            type = ItemType.WIDGET_BUILTIN,
            container = Container.DESKTOP,
            page = 0,
            cellX = 0,
            cellY = 0,
            spanX = 2,
            spanY = 1,
            builtinKind = KIND_DATE,
        )
        items += WorkspaceItem(
            type = ItemType.WIDGET_BUILTIN,
            container = Container.DESKTOP,
            page = 0,
            cellX = 2,
            cellY = 0,
            spanX = 2,
            spanY = 1,
            builtinKind = KIND_WEATHER,
        )
        items += WorkspaceItem(
            type = ItemType.WIDGET_BUILTIN,
            container = Container.DESKTOP,
            page = 0,
            cellX = 0,
            cellY = 1,
            spanX = profile.gridColumns,
            spanY = 2,
            builtinKind = KIND_MONTH_CALENDAR,
        )
        return items
    }

    /** The dock apps, capped at the grid column count. */
    fun dockOnly(
        profile: DeviceProfile,
        dockAppComponents: List<Pair<String, String>>,
    ): List<WorkspaceItem> =
        dockAppComponents.take(profile.gridColumns).mapIndexed { index, (pkg, component) ->
            WorkspaceItem(
                type = ItemType.APP,
                container = Container.DOCK,
                page = 0,
                cellX = index,
                cellY = 0,
                packageName = pkg,
                componentName = component,
            )
        }

    /**
     * Page 2 app grid (PRD section 9.2). Apps are placed in preference order first
     * (Camera, Galeri, Musik, Peta...), then any remaining apps, in a 4-column grid.
     * Apps that are not installed are skipped.
     */
    private val PREFERRED_PACKAGE_HINTS = listOf(
        "camera", "gallery", "photos", "music", "spotify", "maps", "notes", "keep",
        "clock", "alarm", "calculator", "weather", "contacts", "email", "gmail",
        "files", "downloads", "settings", "vending", "play",
    )

    fun pageTwoApps(
        profile: DeviceProfile,
        apps: List<AppEntry>,
    ): List<WorkspaceItem> {
        val columns = 4
        val ordered = apps
            .sortedBy { app ->
                val lower = app.packageName.lowercase()
                PREFERRED_PACKAGE_HINTS.indexOfFirst { lower.contains(it) }
                    .let { if (it < 0) Int.MAX_VALUE else it }
            }
        return ordered.mapIndexed { index, app ->
            WorkspaceItem(
                type = ItemType.APP,
                container = Container.DESKTOP,
                page = 1,
                cellX = index % columns,
                cellY = index / columns,
                packageName = app.packageName,
                componentName = app.componentName,
                userSerial = app.userSerial,
            )
        }
    }
}
