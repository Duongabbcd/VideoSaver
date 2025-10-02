package com.example.videosaver.screen.bookmark.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.example.videosaver.R
import com.example.videosaver.databinding.BookmarkViewBinding
import com.example.videosaver.databinding.LongBookmarkViewBinding
import com.example.videosaver.remote.model.Bookmark
import com.example.videosaver.screen.browse.BrowseActivity
import com.example.videosaver.screen.home.subscreen.HomeFragment.Companion.checkForInternet

class BookmarkAdapter(private val isActivity: Boolean = false) :
    RecyclerView.Adapter<BookmarkAdapter.MyHolder>() {
      private lateinit var context: Context
      private val allBookmarks =  mutableListOf<Bookmark>()
    private val colors by lazy {
        context.resources.getIntArray(R.array.myColors)
    }
    
    fun submitList(list: List<Bookmark>) {
        allBookmarks.clear()
        allBookmarks.addAll(list)
        notifyDataSetChanged()
    }

    class MyHolder(
        binding: BookmarkViewBinding? = null,
        bindingL: LongBookmarkViewBinding? = null
    ) : RecyclerView.ViewHolder((binding?.root ?: bindingL?.root)!!) {
        val image = (binding?.bookmarkIcon ?: bindingL?.bookmarkIcon)!!
        val name = (binding?.bookmarkName ?: bindingL?.bookmarkName)!!
        val root = (binding?.root ?: bindingL?.root)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        context = parent.context
        if (isActivity)
            return MyHolder(
                bindingL = LongBookmarkViewBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )
        return MyHolder(
            binding = BookmarkViewBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val bookmark = allBookmarks[position]
        try {
            val input = bookmark.imagePath ?: bookmark.image
            Glide.with(context).load(input).placeholder(R.drawable.icon_default_app).error(R.drawable.icon_default_app)
                .into(holder.image)
        } catch (e: Exception) {
            e.printStackTrace()
            holder.image.setBackgroundColor(colors[(colors.indices).random()])
        }

        holder.name.text = bookmark.name


        holder.root.setOnClickListener {
            when {
                checkForInternet(context) -> {

                    context.startActivity(Intent(context, BrowseActivity::class.java ).apply {
                        putExtra("receivedURL", bookmark.url)
                    })
                }

                else -> Snackbar.make(holder.root, "Internet Not Connected\uD83D\uDE03", 3000)
                    .show()
            }

        }
    }

    override fun getItemCount(): Int {
        return allBookmarks.size
    }
}
