package com.example.videosaver.advance.ui.browser.webtab

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import com.example.videosaver.advance.data.local.room.entity.HistoryItem
import com.example.videosaver.advance.ui.component.adapter.SuggestionTabListener
import com.example.videosaver.advance.ui.component.adapter.TabSuggestionAdapter
import com.example.videosaver.advance.ui.webtab.WebTabViewModel
import com.example.videosaver.base.BaseFragment
import com.example.videosaver.databinding.FragmentWebTabBinding


class WebTabFragment : BaseFragment<FragmentWebTabBinding>(FragmentWebTabBinding::inflate){
    private val tabViewModel: WebTabViewModel by viewModels()

    private lateinit var suggestionAdapter: TabSuggestionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        suggestionAdapter =
            TabSuggestionAdapter(requireContext(), mutableListOf(), suggestionListener)

        binding.apply {
            viewModel = tabViewModel

            etSearch.setAdapter(suggestionAdapter)
            etSearch.addTextChangedListener(onInputTabChangeListener)
        }
    }


    private val onInputTabChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            val input = s.toString()

            tabViewModel.showTabSuggestions()
            tabViewModel.tabPublishSubject.onNext(input)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    private val suggestionListener = object : SuggestionTabListener {
        override fun onItemClicked(suggestion: HistoryItem) {
            tabViewModel.loadPage(suggestion.url)
        }
    }



    override fun shareWebLink() {
    }

    override fun bookmarkCurrentUrl() {
    }

    companion object {
        fun newInstance() = WebTabFragment()
    }


}