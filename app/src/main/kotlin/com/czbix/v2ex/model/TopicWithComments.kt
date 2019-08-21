package com.czbix.v2ex.model

class TopicWithComments(
        val topic: Topic,
        val comments: List<Comment>,
        val curPage: Int,
        val maxPage: Int,
        val csrfToken: String?,
        val onceToken: String?
) {
    var lastReadPos: Int = 0
}
