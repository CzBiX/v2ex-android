package com.czbix.v2ex.model

import com.czbix.v2ex.db.CommentAndMember

class TopicResponse(
        val topic: Topic,
        val comments: List<CommentAndMember>,
        val curPage: Int,
        val maxPage: Int,
        val csrfToken: String?,
        val onceToken: String?
) {
    val nextPage = if (curPage < maxPage) curPage + 1 else 0
}
