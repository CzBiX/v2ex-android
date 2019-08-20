package com.czbix.v2ex.model

import com.czbix.v2ex.ui.adapter.CommentController

class TopicWithComments(
        val topic: Topic,
        val comments: List<Comment>,
        val curPage: Int,
        val maxPage: Int,
        val csrfToken: String?,
        val onceToken: String?,
        val topicBlocks: List<CommentController.ContentBlock>
) {
    var lastReadPos: Int = 0
}
