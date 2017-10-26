package com.czbix.v2ex.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.czbix.v2ex.R
import com.czbix.v2ex.common.PrefStore
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.ui.fragment.NodeListFragment
import com.czbix.v2ex.util.ViewUtils
import com.google.common.base.Strings

class TopicView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {

    val mTitle: TextView
    val mAvatar: AvatarView
    val mUsername: TextView
    val mNode: TextView
    val mReplyCount: TextView
    val mTime: TextView
    val mContent: TextView

    private var mListener: OnTopicActionListener? = null
    private var mTopic: Topic? = null

    init {
        View.inflate(context, R.layout.view_topic, this)

        mAvatar = findViewById(R.id.avatar_img)
        mTitle = findViewById(R.id.title_tv)
        mUsername = findViewById(R.id.username_tv)
        mNode = findViewById(R.id.node_tv)
        mTime = findViewById(R.id.time_tv)
        mReplyCount = findViewById(R.id.reply_count_tv)
        mContent = findViewById(R.id.content)

        if (PrefStore.getInstance().isContentSelectable) {
            mContent.setTextIsSelectable(true)
        }
    }

    fun setListener(listener: OnTopicActionListener) {
        mListener = listener

        setOnClickListener(this)
    }

    fun setContentListener(listener: HtmlMovementMethod.OnHtmlActionListener) {
        mContent.movementMethod = HtmlMovementMethod(listener)
    }

    fun setNodeListener(listener: NodeListFragment.OnNodeActionListener) {
        mNode.setOnClickListener { v -> listener.onNodeOpen(mTopic!!.node) }
    }

    fun setAvatarListener(listener: AvatarView.OnAvatarActionListener) {
        val tmp = { v: View -> listener.onMemberClick(mTopic!!.member) }

        mAvatar.setOnClickListener(tmp)
        mUsername.setOnClickListener(tmp)
    }

    fun fillData(topic: Topic) {
        if (!topic.hasInfo()) {
            visibility = View.INVISIBLE
            return
        }
        visibility = View.VISIBLE
        mTopic = topic

        updateForRead()

        ViewUtils.setHtmlIntoTextView(mTitle, topic.title,
                ViewUtils.getDimensionPixelSize(R.dimen.abc_text_size_body_1_material), false)
        mUsername.text = topic.member.username
        mNode.text = String.format("› %s", topic.node.title)
        mTime.text = topic.replyTime
        val replyCount = topic.replyCount
        if (replyCount > 0) {
            mReplyCount.visibility = View.VISIBLE
            mReplyCount.text = replyCount.toString()
        } else {
            mReplyCount.visibility = View.INVISIBLE
        }

        mAvatar.setAvatar(topic.member.avatar)
        setContent(topic)
    }

    fun updateForRead() {
        if (mTopic!!.hasRead()) {
            mReplyCount.alpha = 0.3f
        } else {
            mReplyCount.alpha = 1f
        }
    }

    private fun setContent(topic: Topic) {
        val content = topic.content
        if (Strings.isNullOrEmpty(content)) {
            mContent.visibility = View.GONE
            return
        }
        mContent.visibility = View.VISIBLE
        ViewUtils.setHtmlIntoTextView(mContent, content, ViewUtils.getWidthPixels() - TOPIC_PICTURE_OTHER_WIDTH, true)
    }

    override fun onClick(v: View) {
        mListener!!.onTopicOpen(v, mTopic!!)
        updateForRead()
    }

    interface OnTopicActionListener {
        fun onTopicOpen(view: View, topic: Topic)
    }

    companion object {
        private val TOPIC_PICTURE_OTHER_WIDTH = ViewUtils.getDimensionPixelSize(R.dimen.topic_picture_other_width)
    }
}
