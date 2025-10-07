package com.example.videosaver.screen.home.subscreen.process.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.videosaver.remote.process.DownloadItem
import com.example.videosaver.R
import com.example.videosaver.remote.process.DownloadStatus

class DownloadAdapter(
    private val onPause: (DownloadItem) -> Unit,
    private val onResume: (DownloadItem) -> Unit,
    private val onCancel: (DownloadItem) -> Unit
) : ListAdapter<DownloadItem, DownloadAdapter.DownloadViewHolder>(DiffCallback()) {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_download, parent, false)
        return DownloadViewHolder(view)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DownloadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: DownloadItem) {
            itemView.findViewById<TextView>(R.id.fileNameText).text = item.fileName
            val progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
            val percentText = itemView.findViewById<TextView>(R.id.progressText)
            val pauseBtn = itemView.findViewById<ImageView>(R.id.pauseBtn)
            val cancelBtn = itemView.findViewById<ImageView>(R.id.cancelBtn)

            progressBar.progress = item.progress
            percentText.text = "${item.progress}%"

            val displayedIcon = when (item.status) {
                DownloadStatus.Downloading -> R.drawable.icon_pause
                DownloadStatus.Paused, DownloadStatus.Failed -> R.drawable.icon_play
                else -> R.drawable.icon_play
            }

            Glide.with(context).load(displayedIcon).into(pauseBtn)

            pauseBtn.setOnClickListener {
                if (item.status == DownloadStatus.Downloading) onPause(item)
                else onResume(item)
            }

            cancelBtn.setOnClickListener {
                onCancel(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DownloadItem>() {
        override fun areItemsTheSame(old: DownloadItem, new: DownloadItem) = old.url == new.url
        override fun areContentsTheSame(old: DownloadItem, new: DownloadItem) = old == new
    }
}
