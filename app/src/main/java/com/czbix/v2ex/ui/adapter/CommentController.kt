package com.czbix.v2ex.ui.adapter

import android.view.View
import androidx.core.view.updatePaddingRelative
import com.airbnb.epoxy.*
import com.czbix.v2ex.R
import com.czbix.v2ex.databinding.ViewPostscriptBinding
import com.czbix.v2ex.model.Comment
import com.czbix.v2ex.model.Member
import com.czbix.v2ex.model.Postscript
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.ui.fragment.NodeListFragment.OnNodeActionListener
import com.czbix.v2ex.ui.widget.AvatarView.OnAvatarActionListener
import com.czbix.v2ex.ui.widget.CommentView
import com.czbix.v2ex.ui.widget.CommentView.OnCommentActionListener
import com.czbix.v2ex.ui.widget.DividerItemDecoration
import com.czbix.v2ex.ui.widget.HtmlMovementMethod
import com.czbix.v2ex.ui.widget.HtmlMovementMethod.OnHtmlActionListener
import com.czbix.v2ex.ui.widget.TopicView
import com.czbix.v2ex.util.ViewUtils

class CommentController(
        private val mCommentListener: OnCommentActionListener,
        private val mContentListener: OnHtmlActionListener,
        private val mNodeListener: OnNodeActionListener,
        private val mAvatarListener: OnAvatarActionListener
) : EpoxyController() {
    private lateinit var topic: Topic
    private var author: Member? = null
    private var commentList: List<Comment>? = null

    fun setTopic(topic: Topic) {
        this.topic = topic
        requestDelayedModelBuild(1000)
    }

    fun setData(author: Member?, comments: List<Comment>?) {
        this.author = author
        this.commentList = comments
        requestModelBuild()
    }

    override fun buildModels() {
        if (!topic.hasInfo()) {
            return
        }

        commentControllerTopic {
            id("topic")
            contentListener(mContentListener)
            nodeListener(mNodeListener)
            avatarListener(mAvatarListener)
            topic(topic)
            hasPostscript(topic.postscripts?.run { size > 0 } ?: false)
        }

        topic.postscripts?.run {
            val lastIndex = size - 1
            forEachIndexed { index, postscript ->
                commentControllerPostscript {
                    id(index)
                    index(index)
                    postscript(postscript)
                    contentListener(mContentListener)
                    isLastPostscript(index == lastIndex)
                }
            }
        }

        commentList?.forEachIndexed { index, comment ->
            commentControllerComment {
                id(comment.id)
                listener(mCommentListener)
                comment(comment)
                isAuthor(comment.member.isSameUser(author!!))
                position(index)
            }
        }
    }

    @EpoxyModelClass(layout = R.layout.view_topic)
    abstract class TopicModel : EpoxyModelWithHolder<TopicModel.Holder>() {
        @EpoxyAttribute
        lateinit var topic: Topic

        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        lateinit var contentListener: OnHtmlActionListener

        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        lateinit var nodeListener: OnNodeActionListener

        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        lateinit var avatarListener: OnAvatarActionListener

        @EpoxyAttribute
        var hasPostscript = false

        override fun bind(holder: Holder) {
            holder.view.fillData(topic)
            DividerItemDecoration.setHasDecoration(holder.view, !hasPostscript)
        }

        inner class Holder : EpoxyHolder() {
            lateinit var view: TopicView

            override fun bindView(itemView: View) {
                view = itemView as TopicView
                view.updatePaddingRelative(top = view.paddingTop * 2)

                view.setContentListener(contentListener)
                view.setNodeListener(nodeListener)
                view.setAvatarListener(avatarListener)
            }
        }
    }

    @EpoxyModelClass(layout = R.layout.view_postscript)
    abstract class PostscriptModel : EpoxyModelWithHolder<PostscriptModel.Holder>() {
        @EpoxyAttribute
        lateinit var postscript: Postscript

        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        lateinit var contentListener: OnHtmlActionListener

        @EpoxyAttribute
        var index = 0

        @EpoxyAttribute
        var isLastPostscript = false

        override fun bind(holder: Holder) {
            val binding = holder.binding
            val context = holder.view.context
            binding.setTitle(context.getString(R.string.title_postscript, index + 1))
            binding.setTime(postscript.mTime)

            ViewUtils.setHtmlIntoTextView(binding.content, postscript.mContent, 0, true)
            binding.content.movementMethod = HtmlMovementMethod(contentListener)

            DividerItemDecoration.setHasDecoration(holder.view, isLastPostscript)
        }

        inner class Holder : EpoxyHolder() {
            lateinit var binding: ViewPostscriptBinding
            lateinit var view: View

            override fun bindView(itemView: View) {
                view = itemView
                binding = ViewPostscriptBinding.bind(itemView)

                ViewUtils.setSpannableFactory(binding.content)
            }
        }
    }

    @EpoxyModelClass(layout = R.layout.view_comment)
    abstract class CommentModel : EpoxyModelWithHolder<CommentModel.Holder>() {
        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        lateinit var listener: OnCommentActionListener

        @EpoxyAttribute
        lateinit var comment: Comment

        @EpoxyAttribute
        var isAuthor = false

        @EpoxyAttribute
        var position = 0

        override fun bind(holder: Holder) {
            holder.view.fillData(comment, isAuthor, position)
        }

        inner class Holder : EpoxyHolder() {
            lateinit var view: CommentView

            override fun bindView(itemView: View) {
                view = itemView as CommentView

                view.setListener(listener)
            }
        }
    }
}
