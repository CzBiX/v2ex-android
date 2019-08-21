package com.czbix.v2ex.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

abstract class ContentBlock : Parcelable {
    abstract val id: Int
}

@Parcelize
class TextBlock(override val id: Int, val text: CharSequence) : ContentBlock()

@Parcelize
class ImageBlock(override val id: Int, val source: String) : ContentBlock()
