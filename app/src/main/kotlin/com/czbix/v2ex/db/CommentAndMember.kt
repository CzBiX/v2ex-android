package com.czbix.v2ex.db

import androidx.room.Embedded

data class CommentAndMember(
        @Embedded
        val comment: Comment,

        @Embedded(prefix = "member_")
        val member: Member
)

