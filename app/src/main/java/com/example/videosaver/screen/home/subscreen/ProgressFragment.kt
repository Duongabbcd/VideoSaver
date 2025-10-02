package com.example.videosaver.screen.home.subscreen

import android.os.Bundle
import android.view.View
import com.example.videosaver.base.BaseFragment
import com.example.videosaver.databinding.FragmentProgressBinding


class ProgressFragment : BaseFragment<FragmentProgressBinding>(FragmentProgressBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProgressFragment().apply { }
    }
}