package com.example.videosaver.advance.ui.browser.hometab

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.example.videosaver.advance.BrowserListener
import com.example.videosaver.advance.TabManagerProvider
import com.example.videosaver.advance.data.local.model.Suggestion
import com.example.videosaver.advance.ui.component.adapter.SuggestionAdapter
import com.example.videosaver.advance.ui.component.adapter.SuggestionListener
import com.example.videosaver.advance.ui.webtab.WebTabFactory
import com.example.videosaver.base.BaseFragment
import com.example.videosaver.databinding.FragmentBrowserHomeBinding
import kotlinx.coroutines.launch

interface BrowserHomeListener : BrowserListener {

    override fun onBrowserReloadClicked() {
    }

    override fun onTabCloseClicked() {
    }

    override fun onBrowserStopClicked() {
    }

    override fun onBrowserBackClicked() {
    }

    override fun onBrowserForwardClicked() {
    }
}

class BrowserHomeFragment : BaseFragment<FragmentBrowserHomeBinding>(FragmentBrowserHomeBinding::inflate) {
    private lateinit var suggestionAdapter: SuggestionAdapter
    private val homeViewModel: BrowserHomeViewModel by viewModels()
    private lateinit var openPageIProvider: TabManagerProvider


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        suggestionAdapter = SuggestionAdapter(requireContext(), emptyList(), suggestionListener)
        
        binding.apply {
            homeEtSearch.apply {
                setAdapter(suggestionAdapter)
                 addTextChangedListener(onInputHomeSearchChangeListener)
                 imeOptions = EditorInfo.IME_ACTION_DONE
                 setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                         clearFocus()
                        viewModel?.viewModelScope?.launch {
                            val inputText = (homeEtSearch as EditText).text.toString()
                            homeEtSearch.text.clear()
                            openNewTab(inputText)
                        }
                        false
                    } else false
                }
            }
        }
    }

    private val onInputHomeSearchChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            val input = s.toString()
            homeViewModel.searchTextInput.set(input)
            if (!(input.startsWith("http://") || input.startsWith("https://"))) {
                homeViewModel.showSuggestions()
            }
            homeViewModel.homePublishSubject.onNext(input)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    private val suggestionListener = object : SuggestionListener {
        override fun onItemClicked(suggestion: Suggestion) {
            openNewTab(suggestion.content)
        }
    }

    private fun openNewTab(input: String) {
        if (input.isNotEmpty()) {
            openPageIProvider.getOpenTabEvent().value = WebTabFactory.createWebTabFromInput(input)
        }
    }


    override fun shareWebLink(){}

    override fun bookmarkCurrentUrl(){}


    companion object {
        fun newInstance() = BrowserHomeFragment()
    }
}