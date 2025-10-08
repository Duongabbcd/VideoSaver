package com.example.videosaver.advance.ui

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.GridView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import com.example.videosaver.advance.data.local.model.Suggestion
import com.example.videosaver.advance.data.local.room.entity.HistoryItem
import com.example.videosaver.advance.data.local.room.entity.PageInfo
import com.example.videosaver.advance.ui.component.adapter.TopPageAdapter
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.MaterialAutoCompleteTextView

@BindingAdapter("items")
fun setAutoCompleteSuggestions(
    view: MaterialAutoCompleteTextView,
    liveData: LiveData<List<Suggestion>>?
) {
    val items = liveData?.value
    if (items.isNullOrEmpty()) {
        view.setAdapter(null)
        return
    }
    val adapter = ArrayAdapter(
        view.context,
        android.R.layout.simple_dropdown_item_1line,
        items.map { it.content }  // adapt this to your Suggestion class
    )
    view.setAdapter(adapter)
}

@BindingAdapter("items")
fun setItems(dropdown: MaterialAutoCompleteTextView, items: List<HistoryItem>?) {
    if (items == null) return

    val context: Context = dropdown.context

    // Create an ArrayAdapter with a simple dropdown layout and the list of items as Strings
    val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, items.map { it.toString() })

    dropdown.setAdapter(adapter)
}


@BindingAdapter("app:srcCompat")
fun setSrcCompat(view: ShapeableImageView, resId: ObservableInt?) {
    if (resId == null) return
    view.setImageResource(resId.get())
}

//@BindingAdapter("items")
//fun bindGridViewItems(gridView: GridView, items: List<PageInfo>?) {
//    val adapter = gridView.adapter as? BaseAdapter
//    if (adapter == null && items != null) {
//        gridView.adapter = TopPageAdapter(gridView.context, items)
//    } else if (adapter != null) {
//        (adapter as? TopPageAdapter)?.submitList(items)
//    }
//}

