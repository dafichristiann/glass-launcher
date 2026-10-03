package com.example.glasslauncher.data.workspace

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A placed item on the workspace (PRD section 11).
 *
 * A cell position is meaningful for APP / FOLDER / WIDGET items; built-in widgets also carry a
 * [builtinKind] ("date", "weather", "month_calendar").
 */
@Entity(tableName = "workspace_item")
data class WorkspaceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: ItemType,
    val container: Container,
    val containerId: Long? = null,
    val page: Int,
    val cellX: Int,
    val cellY: Int,
    val spanX: Int = 1,
    val spanY: Int = 1,
    val packageName: String? = null,
    val componentName: String? = null,
    val userSerial: Long? = null,
    val title: String? = null,
    val widgetId: Int? = null,
    val builtinKind: String? = null,
)
