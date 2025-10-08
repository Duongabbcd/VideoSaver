package com.example.videosaver.advance.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.videosaver.advance.data.local.room.dao.ConfigDao
import com.example.videosaver.advance.data.local.room.dao.PageDao
import com.example.videosaver.advance.data.local.room.dao.ProgressDao
import com.example.videosaver.advance.data.local.room.dao.HistoryDao
import com.example.videosaver.advance.data.local.room.dao.VideoDao
import com.example.videosaver.advance.data.local.room.entity.DownloadUrlsConverter
import com.example.videosaver.advance.data.local.room.entity.FormatsConverter
import com.example.videosaver.advance.data.local.room.entity.HistoryItem
import com.example.videosaver.advance.data.local.room.entity.ProgressInfo
import com.example.videosaver.advance.data.local.room.entity.SupportedPage
import com.example.videosaver.advance.data.local.room.entity.VideoInfo
import com.example.videosaver.advance.data.local.room.entity.PageInfo

const val DB_VERSION = 1
@Database(
    entities = [
        PageInfo::class,
        SupportedPage::class,
        VideoInfo::class,
        ProgressInfo::class,
        HistoryItem::class],
    version = DB_VERSION,
)

@TypeConverters(FormatsConverter::class, DownloadUrlsConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun configDao(): ConfigDao

    abstract fun videoDao(): VideoDao

    abstract fun progressDao(): ProgressDao

    abstract fun pageDao(): PageDao

    abstract fun historyDao(): HistoryDao

}