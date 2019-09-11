package com.czbix.v2ex.ui

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("visibility")
fun View.setVisibility(value: Boolean) {
    visibility = if (value) View.VISIBLE else View.GONE
}

@BindingAdapter("visible")
fun View.setVisible(value: Boolean) {
    visibility = if (value) View.VISIBLE else View.INVISIBLE
}
