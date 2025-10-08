package com.example.videosaver.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.videosaver.screen.home.MainActivity

abstract class BaseFragment<T : ViewBinding>(private val inflate: Inflate<T>) : Fragment() {
     val binding: T by lazy { inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isAdded && activity == null) {
            return
        }
        reloadData(false)
    }

    open fun reloadData(isDisplayLoading: Boolean) {
        if (MainActivity.Companion.isChangeTheme) {
            return
        }
    }

    abstract fun shareWebLink()

    abstract fun bookmarkCurrentUrl()



    protected inline fun withSafeContext(action: (Context) -> Unit) {
        if (!isAdded || context == null) return
        val ctx = requireContext()
        action(ctx)
    }
}
