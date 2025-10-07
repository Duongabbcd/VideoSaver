package com.example.videosaver.screen.home.subscreen.saved.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.videosaver.databinding.ItemVideoSolutionBinding
import com.example.videosaver.viewmodel.video.MediaFile


class SavedVideoAdapter(
    private val onClickListener: (MediaFile) -> Unit
) : ListAdapter<MediaFile, SavedVideoAdapter.SavedVideoViewHolder>(DiffCallback()) {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedVideoViewHolder {
        context = parent.context
        return SavedVideoViewHolder(
            ItemVideoSolutionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SavedVideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SavedVideoViewHolder(private val binding: ItemVideoSolutionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MediaFile) {
            binding.apply {
                solution.text = item.name

                root.setOnClickListener {
                    onClickListener(item)
                }
            }

        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MediaFile>() {
        override fun areItemsTheSame(old: MediaFile, new: MediaFile) = old.uri == new.uri
        override fun areContentsTheSame(old: MediaFile, new: MediaFile) = old == new
    }
}
