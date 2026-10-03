package com.example.glasslauncher.data.workspace

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists and exposes the home layout (PRD section 11).
 *
 * On first run it writes a default layout that matches the reference design: page 1 holds the
 * built-in widgets in fixed cells; the dock holds the resolved role apps. Later phases mutate it.
 */
class WorkspaceRepository(
    private val dao: WorkspaceDao,
) {
    /** All items as a Flow, grouped by page for rendering. */
    val items: Flow<List<WorkspaceItem>> = dao.observeAll()

    val desktopByPage: Flow<Map<Int, List<WorkspaceItem>>> = items.map { list ->
        list.filter { it.container == Container.DESKTOP }
            .groupBy { it.page }
    }

    val dockItems: Flow<List<WorkspaceItem>> = items.map { list ->
        list.filter { it.container == Container.DOCK }.sortedBy { it.cellX }
    }

    suspend fun ensureInitialized(default: List<WorkspaceItem>) {
        if (dao.count() == 0) {
            dao.insertAll(default)
        }
    }

    /** Insert the built-in widgets only if no desktop widgets exist yet. */
    suspend fun ensureWidgetsInitialized(widgets: List<WorkspaceItem>) {
        if (dao.desktopWidgetCount() == 0) {
            dao.insertAll(widgets)
        }
    }

    /** Insert the dock items only if the dock is empty. */
    suspend fun ensureDockInitialized(dock: List<WorkspaceItem>) {
        if (dao.dockCount() == 0 && dock.isNotEmpty()) {
            dao.insertAll(dock)
        }
    }

    /** Insert page-2 app grid items only if no desktop app items exist yet. */
    suspend fun ensurePageTwoInitialized(apps: List<WorkspaceItem>) {
        if (dao.desktopAppCount() == 0 && apps.isNotEmpty()) {
            dao.insertAll(apps)
        }
    }

    suspend fun dockCount(): Int = dao.dockCount()

    suspend fun desktopAppCount(): Int = dao.desktopAppCount()

    suspend fun add(item: WorkspaceItem): Long = dao.insert(item)

    /** Remove items referencing a package that is no longer installed (PRD section 11). */
    suspend fun deleteByPackage(packageName: String) = dao.deleteByPackage(packageName)
}
