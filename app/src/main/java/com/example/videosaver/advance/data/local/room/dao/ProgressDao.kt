package com.example.videosaver.advance.data.local.room.dao

import androidx.room.*
import com.example.videosaver.advance.data.local.room.entity.ProgressInfo
import io.reactivex.rxjava3.core.Flowable

@Dao
interface ProgressDao {

    @Query("SELECT * FROM ProgressInfo")
    fun getProgressInfos(): Flowable<List<ProgressInfo>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertProgressInfo(progressInfo: ProgressInfo)

    @Delete
    fun deleteProgressInfo(progressInfo: ProgressInfo)
}