package com.example.glasslauncher.data.workspace

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {

    @Query("SELECT * FROM workspace_item ORDER BY page, cellY, cellX")
    fun observeAll(): Flow<List<WorkspaceItem>>

    @Query("SELECT COUNT(*) FROM workspace_item")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM workspace_item WHERE type = 'WIDGET_BUILTIN'")
    suspend fun desktopWidgetCount(): Int

    @Query("SELECT COUNT(*) FROM workspace_item WHERE container = 'DOCK'")
    suspend fun dockCount(): Int

    @Query("SELECT COUNT(*) FROM workspace_item WHERE container = 'DESKTOP' AND type = 'APP'")
    suspend fun desktopAppCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WorkspaceItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WorkspaceItem): Long

    @Query("DELETE FROM workspace_item")
    suspend fun clear()

    @Query("DELETE FROM workspace_item WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)
}
