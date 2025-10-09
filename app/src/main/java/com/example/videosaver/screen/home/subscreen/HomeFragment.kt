package com.example.videosaver.screen.home.subscreen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.videosaver.base.BaseFragment
import com.example.videosaver.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.example.videosaver.remote.model.Tab
import com.example.videosaver.screen.bookmark.BookmarkActivity
import com.example.videosaver.screen.bookmark.adapter.BookmarkAdapter
import com.example.videosaver.screen.browse.BrowseActivity
import com.example.videosaver.screen.home.MainActivity
import com.example.videosaver.screen.home.MainActivity.Companion.myPager
import com.example.videosaver.utils.Common.gone
import com.example.videosaver.utils.Common.visible

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate), DisplayURL {
    private lateinit var mainActivityRef : MainActivity
    private lateinit var bookmarkAdapter: BookmarkAdapter 
    
    override fun onResume() {
        super.onResume()
        bookmarkAdapter = BookmarkAdapter()
        mainActivityRef = requireActivity() as MainActivity

//        tabsBtn.text = "0"

//        mainActivityRef.binding.topSearchBar.setText("")
//        binding.searchView.setText("")
//        mainActivityRef.binding.webIcon.setImageResource(R.drawable.ic_search)
//
//        mainActivityRef.binding.refreshBtn.visibility = View.GONE

        binding.searchView.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {

                val query = binding.searchView.text.toString().trim()

                if (query.isNotEmpty()) {
                    if (checkForInternet(requireContext())) {
                        startActivity(Intent(requireContext(), BrowseActivity::class.java).apply {
                            putExtra("receivedURL",   query)
                        })
                    } else {
                        Snackbar.make(binding.root, "Internet Not Connected 😃", 3000).show()
                    }
                }

                true // consume the action
            } else {
                false
            }
        }

        binding.searchVideo.setOnClickListener {
            val query = binding.searchView.text.toString().trim()
            if (query.isNotEmpty()) {
                if (checkForInternet(requireContext())) {
                    startActivity(Intent(requireContext(), BrowseActivity::class.java).apply {
                        putExtra("receivedURL",   query)
                    })
                } else {
                    Snackbar.make(binding.root, "Internet Not Connected 😃", 3000).show()
                }
            }
        }

        binding.searchView.addTextChangedListener( object: TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                //do nothing
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val input = s.toString()
                if(input.isNotEmpty())  {
                    binding.searchBtn.visible()
                    binding.searchBtn.setOnClickListener {
                        binding.searchView.setText("")
                    }
                } else {
                    binding.searchBtn.gone()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //do nothing
            }

        })


        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.setItemViewCacheSize(5)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.recyclerView.adapter = bookmarkAdapter
        bookmarkAdapter.submitList(MainActivity.bookmarkList.take(10))

        if(MainActivity.bookmarkList.size < 1)
            binding.viewAllBtn.visibility = View.GONE
        binding.viewAllBtn.setOnClickListener {
            startActivity(Intent(requireContext(), BookmarkActivity::class.java))
        }
    }

    override fun onReceivedURL(url: String) {
        binding.searchView.setText(url)
        binding.searchVideo.setOnClickListener {
            startActivity(Intent(requireContext(), BrowseActivity::class.java).apply {
                putExtra("receivedURL", url)
            })
        }
    }

    override fun shareWebLink(){}

    override fun bookmarkCurrentUrl(){}

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment().apply { }


        @SuppressLint("NotifyDataSetChanged")
        fun changeTab(url: String, fragment: Fragment, isBackground: Boolean = false) {
            return
            MainActivity.tabsList.add(Tab(name = url, fragment = fragment))
            myPager.adapter?.notifyDataSetChanged()
//            tabsBtn.text = "0"

            if (!isBackground) myPager.currentItem = MainActivity.tabsList.size - 1
        }

        @Suppress("DEPRECATION")
        fun checkForInternet(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo ?: return false
                networkInfo.isConnected
            }
        }
    }


}

interface DisplayURL {
    fun onReceivedURL(url: String)
}