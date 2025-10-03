package com.example.videosaver.screen.download.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videosaver.databinding.ItemVideoSolutionBinding
import com.example.videosaver.remote.model.scraper.VideoSolution
import com.example.videosaver.R
import com.example.videosaver.utils.Utils
import com.example.videosaver.utils.Utils.getFileSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoSolutionAdapter(private val onClickListener: (VideoSolution) -> Unit) :
    RecyclerView.Adapter<VideoSolutionAdapter.VideoSolutionViewHolder>() {
    private lateinit var context: Context
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

    fun submitList(input: List<VideoSolution>) {
        allVideoSolutions.clear()
        allVideoSolutions.addAll(input)
        notifyDataSetChanged()
    }

    inner class VideoSolutionViewHolder(private val binding: ItemVideoSolutionBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(position: Int) {
                val videoSolution = allVideoSolutions[position]

                binding.apply {
                    val videoURL = if(videoSolution.url.isEmpty()) videoSolution.manifest_url else videoSolution.url
                    CoroutineScope(Dispatchers.IO).launch {
                        val size = getFileSize(videoURL)
                        println("File size: $size bytes")
                        // update UI here if needed
                        withContext(Dispatchers.Main) {
                            binding.size.text = Utils.convertIntoFileSize(size)
                        }
                    }

                    val displayIcon = when {
                        videoSolution.audio_ext.isNotEmpty() -> R.drawable.icon_audio
                        videoSolution.video_ext.isNotEmpty() -> R.drawable.icon_video
                       else -> R.drawable.icon_audio
                    }

                    icon.setImageResource(displayIcon)
                    solution.text =  displaySolution(videoSolution)

                    root.setOnClickListener {
                        onClickListener(videoSolution)
                    }
                }
            }

        private fun displaySolution(solution: VideoSolution) : String {
            return when{
                solution.format.contains("sd", true) -> "640x480"
                solution.format.contains("hd", true) -> "1280x720"
                else -> {
                    if(solution.width > 0 && solution.height > 0) {
                        "${solution.width}x${solution.height}"
                    } else {
                        "SD"
                    }
                }
            }


        }
    }
}