package com.example.videosaver.screen.home.subscreen.process

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.videosaver.base.BaseFragment
import com.example.videosaver.databinding.FragmentProgressBinding
import com.example.videosaver.screen.home.subscreen.process.adapter.DownloadAdapter
import com.example.videosaver.utils.Common.gone
import com.example.videosaver.utils.Common.visible
import com.example.videosaver.utils.advance.util.AppLogger
import com.example.videosaver.viewmodel.process.DownloadViewModel

class ProgressFragment : BaseFragment<FragmentProgressBinding>(FragmentProgressBinding::inflate) {
    private val viewModel: DownloadViewModel by viewModels()

    private lateinit var downloadAdapter: DownloadAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.apply {
            val ctx = context ?: return@apply
            downloadAdapter = DownloadAdapter(
                onPause = { url -> viewModel.pauseDownload(url.fileName) },
                onResume = { url -> viewModel.resumeDownload(ctx, url.url ,url.fileName,isVideo = true) },
                onCancel = { url -> viewModel.cancelDownload(url.fileName) }
            )

            allDownloadProcesses.adapter = downloadAdapter
            allDownloadProcesses.layoutManager = LinearLayoutManager(ctx)
            // Collect download list updates from ViewModel
            lifecycleScope.launchWhenStarted {
                viewModel.downloads.collect { downloads ->
                    if(downloads.isEmpty()) {
                        binding.emptyLayout.visible()
                        binding.allDownloadProcesses.gone()
                    } else {
                        binding.emptyLayout.gone()
                        binding.allDownloadProcesses.visible()

                        downloadAdapter.submitList(downloads)
                    }

                }
            }
        }
    }

    override fun shareWebLink(){}

    override fun bookmarkCurrentUrl(){}

    companion object {
        @JvmStatic
        fun newInstance() = ProgressFragment().apply { }
    }
}

class WrapContentLinearLayoutManager : LinearLayoutManager {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
        context, orientation, reverseLayout
    ) {
    }

    constructor(
        context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            AppLogger.e("meet a IOOBE in RecyclerView")
        }
    }
}