package com.example.videosaver.advance.di.module

import android.content.Context
import androidx.room.Room
import com.example.videosaver.advance.data.local.room.AppDatabase
import com.example.videosaver.advance.data.local.room.dao.ConfigDao
import com.example.videosaver.advance.data.local.room.dao.HistoryDao
import com.example.videosaver.advance.data.local.room.dao.PageDao
import com.example.videosaver.advance.data.local.room.dao.ProgressDao
import com.example.videosaver.advance.data.local.room.dao.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "video_saver.db")
            .build()
    }

    @Provides
    @Singleton
    fun provideConfigDao(database: AppDatabase): ConfigDao = database.configDao()

    @Provides
    @Singleton
    fun provideCommentDao(database: AppDatabase): VideoDao = database.videoDao()

    @Provides
    @Singleton
    fun provideProgressDao(database: AppDatabase): ProgressDao = database.progressDao()

    @Provides
    @Singleton
    fun provideHistoryDao(database: AppDatabase): HistoryDao = database.historyDao()

    @Provides
    @Singleton
    fun providePageDao(database: AppDatabase): PageDao = database.pageDao()
}
