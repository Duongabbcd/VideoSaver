package com.example.videosaver.screen.bookmark

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videosaver.base.BaseActivity
import com.example.videosaver.databinding.ActivityBookmarkBinding
import com.example.videosaver.screen.bookmark.adapter.BookmarkAdapter
import com.example.videosaver.screen.home.MainActivity

class BookmarkActivity : BaseActivity<ActivityBookmarkBinding>(ActivityBookmarkBinding::inflate) {
    private lateinit var bookmarkAdapter: BookmarkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookmarkAdapter = BookmarkAdapter(isActivity = true)

        binding.rvBookmarks.setItemViewCacheSize(5)
        binding.rvBookmarks.hasFixedSize()
        binding.rvBookmarks.layoutManager = LinearLayoutManager(this)
        binding.rvBookmarks.adapter = bookmarkAdapter
        bookmarkAdapter.submitList(MainActivity.bookmarkList)
    }
}