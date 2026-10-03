package com.example.glasslauncher

import com.example.glasslauncher.core.device.DeviceProfile
import com.example.glasslauncher.core.icons.GlassIconRequest
import com.example.glasslauncher.core.icons.cacheKey
import com.example.glasslauncher.data.workspace.Container
import com.example.glasslauncher.data.workspace.DefaultLayout
import com.example.glasslauncher.data.workspace.ItemType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultLayoutTest {

    private fun profile(columns: Int = 5, rows: Int = 5) = DeviceProfile(
        brandModel = "Test",
        androidVersion = 35,
        skin = "AOSP",
        refreshRateHz = 120,
        drawerVertical = true,
        hasDrawer = true,
        gridColumns = columns,
        gridRows = rows,
        gestureNavigation = true,
        searchBarTop = true,
        dockApps = emptyList(),
        frameBudgetHz = 120,
    )

    @Test
    fun `builds date weather calendar widgets and dock apps`() {
        val dock = listOf("phone.pkg" to "phone.pkg/Main", "sms.pkg" to "sms.pkg/Main")
        val items = DefaultLayout.build(profile(), dock)

        val builtins = items.filter { it.type == ItemType.WIDGET_BUILTIN }
        assertEquals(3, builtins.size)
        assertTrue(builtins.any { it.builtinKind == DefaultLayout.KIND_DATE })
        assertTrue(builtins.any { it.builtinKind == DefaultLayout.KIND_WEATHER })
        assertTrue(builtins.any { it.builtinKind == DefaultLayout.KIND_MONTH_CALENDAR })

        val dockItems = items.filter { it.container == Container.DOCK }
        assertEquals(2, dockItems.size)
        assertEquals(0, dockItems[0].cellX)
        assertEquals(1, dockItems[1].cellX)
    }

    @Test
    fun `dock is capped by grid columns`() {
        val dock = (0 until 8).map { "pkg$it" to "pkg$it/Main" }
        val items = DefaultLayout.build(profile(columns = 4), dock)
        assertEquals(4, items.count { it.container == Container.DOCK })
    }

    @Test
    fun `no two desktop items share a cell on page 0`() {
        val items = DefaultLayout.build(profile(), emptyList())
        val cells = items.filter { it.container == Container.DESKTOP }
            .map { Triple(it.page, it.cellX, it.cellY) }
        assertEquals(cells.size, cells.toSet().size)
    }
}

class GlassIconRequestTest {

    @Test
    fun `cache key changes when visual settings change`() {
        val base = GlassIconRequest("pkg", "pkg/Main", 0)
        val differentShape = base.copy(tileSizeDp = 72)
        assertNotEquals(base.cacheKey(), differentShape.cacheKey())
    }

    @Test
    fun `cache key is stable for identical requests`() {
        val a = GlassIconRequest("pkg", "pkg/Main", 0)
        val b = GlassIconRequest("pkg", "pkg/Main", 0)
        assertEquals(a.cacheKey(), b.cacheKey())
    }
}
