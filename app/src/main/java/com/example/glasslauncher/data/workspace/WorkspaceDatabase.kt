package com.example.glasslauncher.data.workspace

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class WorkspaceConverters {
    @TypeConverter fun toContainer(value: String): Container = Container.valueOf(value)
    @TypeConverter fun fromContainer(value: Container): String = value.name
    @TypeConverter fun toItemType(value: String): ItemType = ItemType.valueOf(value)
    @TypeConverter fun fromItemType(value: ItemType): String = value.name
}

@Database(entities = [WorkspaceItem::class], version = 1, exportSchema = false)
@TypeConverters(WorkspaceConverters::class)
abstract class WorkspaceDatabase : RoomDatabase() {
    abstract fun workspaceDao(): WorkspaceDao
}
