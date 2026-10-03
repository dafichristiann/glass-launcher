package com.example.glasslauncher.di

import android.content.Context
import androidx.room.Room
import com.example.glasslauncher.core.device.DeviceDefaults
import com.example.glasslauncher.core.device.DeviceProfile
import com.example.glasslauncher.core.icons.GlassIconRenderer
import com.example.glasslauncher.core.icons.IconCache
import com.example.glasslauncher.core.wallpaper.WallpaperRepository
import com.example.glasslauncher.data.apps.AndroidLauncherAppsSource
import com.example.glasslauncher.data.apps.AppRepository
import com.example.glasslauncher.data.apps.LauncherAppsSource
import com.example.glasslauncher.data.workspace.WorkspaceDao
import com.example.glasslauncher.data.workspace.WorkspaceDatabase
import com.example.glasslauncher.data.workspace.WorkspaceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDeviceProfile(@ApplicationContext context: Context): DeviceProfile =
        DeviceDefaults.resolve(context)

    @Provides
    @Singleton
    fun provideWallpaperRepository(@ApplicationContext context: Context): WallpaperRepository =
        WallpaperRepository(context)

    @Provides
    @Singleton
    fun provideLauncherAppsSource(@ApplicationContext context: Context): LauncherAppsSource =
        AndroidLauncherAppsSource(context)

    @Provides
    @Singleton
    fun provideAppRepository(source: LauncherAppsSource): AppRepository =
        AppRepository(source)

    @Provides
    @Singleton
    fun provideIconCache(@ApplicationContext context: Context): IconCache = IconCache(context)

    @Provides
    @Singleton
    fun provideGlassIconRenderer(): GlassIconRenderer = GlassIconRenderer()

    @Provides
    @Singleton
    fun provideWorkspaceDatabase(@ApplicationContext context: Context): WorkspaceDatabase =
        Room.databaseBuilder(context, WorkspaceDatabase::class.java, "workspace.db")
            .addMigrations()
            .build()

    @Provides
    fun provideWorkspaceDao(db: WorkspaceDatabase): WorkspaceDao = db.workspaceDao()

    @Provides
    @Singleton
    fun provideWorkspaceRepository(dao: WorkspaceDao): WorkspaceRepository =
        WorkspaceRepository(dao)
}
