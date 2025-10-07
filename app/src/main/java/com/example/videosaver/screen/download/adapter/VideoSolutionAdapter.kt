package com.example.videosaver.screen.download.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.videosaver.databinding.ItemVideoSolutionBinding
import com.example.videosaver.remote.model.scraper.VideoSolution
import com.example.videosaver.R
import com.example.videosaver.remote.model.scraper.VideoItem
import com.example.videosaver.utils.Common.gone
import com.example.videosaver.utils.Utils
import com.example.videosaver.utils.Utils.getFileSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoSolutionAdapter(private val onClickListener: (VideoSolution, String) -> Unit) :
    RecyclerView.Adapter<VideoSolutionAdapter.VideoSolutionViewHolder>() {
    private lateinit var context: Context
    private  var videoName = ""
    private val allVideoSolutions = mutableListOf<VideoSolution>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VideoSolutionViewHolder {
        context = parent.context
        return VideoSolutionViewHolder(
            ItemVideoSolutionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: VideoSolutionViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return allVideoSolutions.size
    }

    fun getVideoName(input: String) {
        videoName = input
    }

    fun submitList(input: List<VideoSolution>) {
        allVideoSolutions.clear()
        allVideoSolutions.addAll(input)
        notifyDataSetChanged()
    }

    inner class VideoSolutionViewHolder(private val binding: ItemVideoSolutionBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(position: Int) {
                val videoSolution = allVideoSolutions[position]
                println("VideoSolutionViewHolder: $videoSolution")
                binding.apply {
                    solution.text = videoSolution.quality
                    val videoURL = videoSolution.url
                    CoroutineScope(Dispatchers.IO).launch {
                        val size = getFileSize(videoURL)
                        println("File size: $size bytes")
                        // update UI here if needed
                        withContext(Dispatchers.Main) {
                            binding.size.text = Utils.convertIntoFileSize(videoSolution.size.toLong())
                        }
                    }

                    Glide.with(context).load(R.drawable.icon_video).placeholder(R.drawable.icon_video).error(R.drawable.icon_video).into(binding.icon)


//                    icon.setImageResource(displayIcon)
//                    solution.text =  displaySolution(videoSolution)

                    root.setOnClickListener {
                        onClickListener(videoSolution, videoName)
                    }
                }
            }

    }
}