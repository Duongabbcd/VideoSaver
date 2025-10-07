package com.example.videosaver.screen.home.subscreen.saved

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.videosaver.base.BaseFragment
import com.example.videosaver.databinding.FragmentSavedBinding
import com.example.videosaver.screen.download.adapter.VideoSolutionAdapter
import com.example.videosaver.screen.home.player.PlayerActivity
import com.example.videosaver.screen.home.subscreen.saved.adapter.SavedVideoAdapter
import com.example.videosaver.utils.Common.gone
import com.example.videosaver.utils.Common.visible
import com.example.videosaver.viewmodel.video.VideoViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SavedFragment : BaseFragment<FragmentSavedBinding>(FragmentSavedBinding::inflate) {
    private val viewModel: VideoViewModel by viewModels()

    private val savedVideoAdapter by lazy {

         SavedVideoAdapter(onClickListener = { mediaFile ->
             val ctx = context ?: return@SavedVideoAdapter
             startActivity(Intent(ctx, PlayerActivity::class.java).apply {
                 putExtra("mediaFile", Gson().toJson(mediaFile))
             })
         })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.allMediaFiles.observe(viewLifecycleOwner) { list ->
             allVideoSolutions.adapter = savedVideoAdapter
             if(list.isEmpty()) {
                 binding.allVideoSolutions.gone()
                 binding.emptyLayout.visible()
             } else {
                 binding.allVideoSolutions.visible()
                 binding.emptyLayout.gone()
                list.onEach {
                    println("savedVideoAdapter: $it")
                }
                 savedVideoAdapter.submitList(list)
             }
            }

            viewModel.loading.observe(viewLifecycleOwner) { isDisplayed ->
               binding.progressBarBottomSheet.isVisible = isDisplayed
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val ctx =context ?: return

        viewModel.queryMediaFiles(ctx)
    }

    companion object {
        @JvmStatic
        fun newInstance() = SavedFragment().apply { }

        private val TAG = SavedFragment::class.java.simpleName
    }
}