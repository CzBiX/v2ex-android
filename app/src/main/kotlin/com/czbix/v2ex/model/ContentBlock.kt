package com.czbix.v2ex.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class ContentBlock : Parcelable {
    @Parcelize
    data class TextBlock(val id: Int, val text: CharSequence) : ContentBlock()

    @Parcelize
    data class ImageBlock(val id: Int, val source: String) : ContentBlock()

    @Parcelize
    data class PreBlock(val id: Int, val text: CharSequence) : ContentBlock()
}
