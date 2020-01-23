package com.czbix.v2ex.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.czbix.v2ex.R
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.ui.fragment.NodeListFragment
import com.czbix.v2ex.util.ViewUtils

class TopicView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        RelativeLayout(context, attrs, defStyleAttr), View.OnClickListener {

    val mTitle: TextView
    val mAvatar: AvatarView
    val mUsername: TextView
    val mNode: TextView
    val mReplyCount: TextView
    val mTime: TextView
    val border: View

    private var mListener: OnTopicActionListener? = null
    private lateinit var mTopic: Topic

    init {
        View.inflate(context, R.layout.layout_topic, this)

        mAvatar = findViewById(R.id.avatar_img)
        mTitle = findViewById(R.id.title_tv)
        mUsername = findViewById(R.id.username_tv)
        mNode = findViewById(R.id.node_tv)
        mTime = findViewById(R.id.time_tv)
        mReplyCount = findViewById(R.id.reply_count_tv)
        border = findViewById(R.id.border)

        ViewUtils.setSpannableFactory(mTitle)
    }

    fun setListener(listener: OnTopicActionListener) {
        mListener = listener

        setOnClickListener(this)
    }

    fun setNodeListener(listener: NodeListFragment.OnNodeActionListener) {
        mNode.setOnClickListener { listener.onNodeOpen(mTopic.node) }
    }

    fun setAvatarListener(listener: AvatarView.OnAvatarActionListener) {
        val tmp = { _: View -> listener.onMemberClick(mTopic.member!!) }

        mAvatar.setOnClickListener(tmp)
        mUsername.setOnClickListener(tmp)
    }

    fun fillData(glide: RequestManager, topic: Topic, readed: Boolean) {
        mTopic = topic

        updateForRead(readed)

        ViewUtils.setHtmlIntoTextView(mTitle, topic.title!!,
                ViewUtils.getDimensionPixelSize(R.dimen.abc_text_size_body_1_material), false)
        mUsername.text = topic.member!!.username
        mNode.text = String.format("› %s", topic.node!!.title)
        mTime.text = topic.replyTime
        val replyCount = topic.replyCount
        if (replyCount > 0) {
            mReplyCount.visibility = View.VISIBLE
            mReplyCount.text = replyCount.toString()
        } else {
            mReplyCount.visibility = View.INVISIBLE
        }

        mAvatar.setAvatar(glide, topic.member.avatar)
        border.visibility = if (topic.content.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    fun clear(glide: RequestManager) {
        mListener = null
        mAvatar.setAvatar(glide, null)
    }

    private fun updateForRead(readed: Boolean) {
        if (readed) {
            mReplyCount.alpha = 0.3f
        } else {
            mReplyCount.alpha = 1f
        }
    }

    override fun onClick(v: View) {
        mListener!!.onTopicOpen(v, mTopic)
    }

    interface OnTopicActionListener {
        fun onTopicOpen(view: View, topic: Topic)
    }
}
