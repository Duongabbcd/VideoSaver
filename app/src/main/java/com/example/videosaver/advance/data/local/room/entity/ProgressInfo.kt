package com.example.videosaver.advance.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.videosaver.utils.advance.util.FileUtil.Companion.getFileSizeReadable
import com.example.videosaver.utils.advance.util.RoomConverter
import java.util.UUID
import kotlin.hashCode

@Entity(tableName = "ProgressInfo")
@TypeConverters(RoomConverter::class)
data class ProgressInfo(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),

    var downloadId: Long = 0,

    @TypeConverters(RoomConverter::class)
    var videoInfo: VideoInfo,

    @Deprecated("bytesDownloaded deprecated use progressDownloaded instead")
    var bytesDownloaded: Int = 0,

    @Deprecated("bytesTotal deprecated use progressTotal instead")
    var bytesTotal: Int = 0,

    @ColumnInfo(defaultValue = "0")
    var progressDownloaded: Long = 0,

    @ColumnInfo(defaultValue = "0")
    var progressTotal: Long = 0,

    var downloadStatus: Int = -1,

    var isLive: Boolean = false,

    var isM3u8: Boolean = false,

    var fragmentsDownloaded: Int = 0,

    var fragmentsTotal: Int = 1,

    @ColumnInfo(name = "infoLine")
    var infoLine: String = ""
) {
    // НЕ ТРОГАТЬ VAR!!!! иначе пиздец с миграцией
    var progress: Int = 0
        get() {
            return (progressDownloaded * 100f / progressTotal).toInt()
        }

    // НЕ ТРОГАТЬ VAR!!!! иначе пиздец с миграцией
    var progressSize: String = ""
        get() {
            return getFileSizeReadable(progressDownloaded.toDouble()) + "/" + getFileSizeReadable(progressTotal.toDouble()) + " - $downloadStatusFormatted"
        }

    // НЕ ТРОГАТЬ VAR!!!! иначе пиздец с миграцией
    var downloadStatusFormatted: String = ""
        get() = when (downloadStatus) {
            VideoTaskState.DOWNLOADING -> "downloading"
            VideoTaskState.SUCCESS -> "success"
            VideoTaskState.PAUSE -> "pause"
            VideoTaskState.PENDING -> "pending"
            VideoTaskState.PREPARE -> "prepare"
            VideoTaskState.ENOSPC -> "failed"
            VideoTaskState.ERROR -> "failed"
            else -> "undefined"
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProgressInfo

        if (id != other.id) return false
        if (downloadId != other.downloadId) return false
        if (videoInfo != other.videoInfo) return false
        if (bytesDownloaded != other.bytesDownloaded) return false
        if (bytesTotal != other.bytesTotal) return false
        if (progressDownloaded != other.progressDownloaded) return false
        if (progressTotal != other.progressDownloaded) return false
        if (downloadStatus != other.downloadStatus) return false
        if (isM3u8 != other.isM3u8) return false
        if (fragmentsDownloaded != other.fragmentsDownloaded) return false
        if (fragmentsTotal != other.fragmentsTotal) return false
        if (infoLine != other.infoLine) return false
        if (progress != other.progress) return false
        if (progressSize != other.progressSize) return false
        return downloadStatusFormatted == other.downloadStatusFormatted
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + downloadId.hashCode()
        result = 31 * result + videoInfo.hashCode()
        result = 31 * result + bytesDownloaded
        result = 31 * result + bytesTotal
        result = 31 * result + progressDownloaded.hashCode()
        result = 31 * result + progressTotal.hashCode()
        result = 31 * result + downloadStatus
        result = 31 * result + isM3u8.hashCode()
        result = 31 * result + fragmentsDownloaded
        result = 31 * result + fragmentsTotal
        result = 31 * result + infoLine.hashCode()
        return result
    }
}

object VideoTaskState {
    const val DEFAULT = 0       // 默认状态
    const val PENDING = -1      // 下载排队
    const val PREPARE = 1       // 下载准备中
    const val START = 2         // 开始下载
    const val DOWNLOADING = 3   // 下载中
    const val PROXYREADY = 4    // 视频可以边下边播
    const val SUCCESS = 5       // 下载完成
    const val ERROR = 6         // 下载出错
    const val PAUSE = 7         // 下载暂停
    const val ENOSPC = 8        // 空间不足
    const val CANCELED = 9      // 下载取消
}
