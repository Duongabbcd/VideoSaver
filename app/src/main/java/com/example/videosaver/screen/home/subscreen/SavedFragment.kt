package com.example.videosaver.screen.home.subscreen

import android.os.Bundle
import android.view.View
import com.example.videosaver.base.BaseFragment
import com.example.videosaver.databinding.FragmentSavedBinding

class SavedFragment : BaseFragment<FragmentSavedBinding>(FragmentSavedBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SavedFragment().apply { }
    }
}