package com.czbix.v2ex.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TopicRecord(
        @PrimaryKey
        val id: Int,
        val title: String,
        var lastReadAt: Long,
        var lastReadComment: Int
)

