package com.czbix.v2ex.db

import androidx.paging.DataSource
import androidx.room.*

@Dao
abstract class CommentDao {
    @Query("SELECT Comment.*, Member.username as member_username, Member.baseUrl as member_baseUrl FROM Comment INNER JOIN Member ON Comment.username = Member.username WHERE topicId = :id ORDER BY id ASC")
    abstract fun getCommentsByTopicId(id: Int): DataSource.Factory<Int, CommentAndMember>

    @Delete(entity = Comment::class)
    abstract suspend fun deleteCommentsByPage(page: CommentPage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertComments(comments: List<Comment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMembers(members: List<Member>)

    @Transaction
    open suspend fun updateCommentAndMembers(list: List<CommentAndMember>, page: CommentPage) {
        val comments = MutableList(list.size) {
            list[it].comment
        }
        val members = list.map {
            it.member
        }.toSet().toList()

        deleteCommentsByPage(page)
        insertMembers(members)
        insertComments(comments)
    }

    class CommentPage(
            val topicId: Int,
            val page: Int
    )
}