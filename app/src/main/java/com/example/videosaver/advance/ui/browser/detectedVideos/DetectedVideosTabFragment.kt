package com.example.videosaver.advance.ui.browser.detectedVideos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videosaver.R
import com.example.videosaver.advance.ui.component.adapter.DownloadTabListener
import com.example.videosaver.advance.ui.component.adapter.VideoInfoAdapter
import com.example.videosaver.base.BaseFragment2
import com.example.videosaver.databinding.FragmentDetectedVideosTabBinding
import com.example.videosaver.screen.home.MainActivity
import com.example.videosaver.screen.home.subscreen.process.WrapContentLinearLayoutManager
import com.example.videosaver.utils.advance.util.AppUtil
import javax.inject.Inject

class DetectedVideosTabFragment : BaseFragment2() {
    var detectedVideosTabViewModel: VideoDetectionTabViewModel? = null
    var candidateFormatListener: DownloadTabListener? = null

    @Inject
    lateinit var mainActivity: MainActivity

    @Inject
    lateinit var appUtil: AppUtil

    private lateinit var binding: FragmentDetectedVideosTabBinding

    private lateinit var layoutMngr: WrapContentLinearLayoutManager

    companion object {
        fun newInstance() = DetectedVideosTabFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (detectedVideosTabViewModel == null || candidateFormatListener == null) {
            Toast.makeText(context, "Something went wrong, try again.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        val adapter = detectedVideosTabViewModel?.let {
            candidateFormatListener?.let { it1 ->
                VideoInfoAdapter(
                   emptyList(),
                    it,
                    it1,
                    appUtil,
                )
            }
        }

        detectedVideosTabViewModel?.let {
            it.detectedVideosList.observe(viewLifecycleOwner) { data ->
                adapter?.setData(data)
            }
        }

        layoutMngr = WrapContentLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding = FragmentDetectedVideosTabBinding.inflate(inflater, container, false).apply {
            title.text = getString(
                R.string.found_videos_from,
                detectedVideosTabViewModel?.webTabModel?.getTabTextInput()?.get()
            ).split("?").firstOrNull()
            detectedVideosTabContainer.setBackgroundColor(getThemeBackgroundColor())
            viewModel = detectedVideosTabViewModel
            videoInfoList.layoutManager = layoutMngr
            videoInfoList.isNestedScrollingEnabled = true
            videoInfoList.adapter = adapter
            dialogListener = candidateFormatListener
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }
}