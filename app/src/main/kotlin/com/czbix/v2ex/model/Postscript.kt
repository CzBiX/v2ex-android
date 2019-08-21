package com.czbix.v2ex.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Postscript(
        val content: List<ContentBlock>,
        val time: String
) : Parcelable
