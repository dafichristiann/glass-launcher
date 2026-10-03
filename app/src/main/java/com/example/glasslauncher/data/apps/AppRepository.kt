package com.example.glasslauncher.data.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale

/**
 * Single source of installed apps (PRD section 5).
 *
 * Nothing else touches LauncherApps. Install/uninstall events re-query the list and emit.
 * Entries are sorted by a locale-aware label collator.
 */
class AppRepository(
    private val source: LauncherAppsSource,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
    val apps: StateFlow<List<AppEntry>> = _apps.asStateFlow()

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageAdded(packageName: String?, user: UserHandle?) = refresh()
        override fun onPackageRemoved(packageName: String?, user: UserHandle?) = refresh()
        override fun onPackageChanged(packageName: String?, user: UserHandle?) = refresh()
        override fun onPackagesAvailable(
            packageNames: Array<out String>?,
            user: UserHandle?,
            replacing: Boolean,
        ) = refresh()
        override fun onPackagesUnavailable(
            packageNames: Array<out String>?,
            user: UserHandle?,
            replacing: Boolean,
        ) = refresh()
    }

    fun start() {
        source.registerCallback(callback)
        refresh()
    }

    fun stop() {
        source.unregisterCallback(callback)
    }

    fun launch(app: AppEntry) {
        source.launchApp(app.componentName, app.userSerial)
    }

    private fun refresh() {
        scope.launch {
            val sorted = withContext(Dispatchers.Default) {
                val collator = Collator.getInstance(Locale.getDefault())
                source.queryLaunchableApps().sortedWith { a, b -> collator.compare(a.label, b.label) }
            }
            _apps.value = sorted
        }
    }
}
