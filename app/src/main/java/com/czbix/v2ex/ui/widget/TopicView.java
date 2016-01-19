package com.czbix.v2ex.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.fragment.NodeListFragment;
import com.czbix.v2ex.ui.helper.ForceTouchDetector;
import com.czbix.v2ex.util.LogUtils;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Strings;

public class TopicView extends FrameLayout implements View.OnClickListener, View.OnTouchListener {
    private static final int TOPIC_PICTURE_OTHER_WIDTH = ViewUtils.getDimensionPixelSize(R.dimen.topic_picture_other_width);

    public final TextView mTitle;
    public final AvatarView mAvatar;
    public final TextView mUsername;
    public final TextView mNode;
    public final TextView mReplyCount;
    public final TextView mTime;
    public final TextView mContent;

    private OnTopicActionListener mListener;
    private ForceTouchDetector mTouchDetector;
    private Topic mTopic;

    public TopicView(Context context) {
        this(context, null);
    }

    public TopicView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_topic, this);

        mAvatar = (AvatarView) findViewById(R.id.avatar_img);
        mTitle = (TextView) findViewById(R.id.title_tv);
        mUsername = (TextView) findViewById(R.id.username_tv);
        mNode = (TextView) findViewById(R.id.node_tv);
        mTime = (TextView) findViewById(R.id.time_tv);
        mReplyCount = (TextView) findViewById(R.id.reply_count_tv);
        mContent = (TextView) findViewById(R.id.content);
    }

    public void setListener(@NonNull OnTopicActionListener listener) {
        mListener = listener;

        setOnClickListener(this);
        setOnTouchListener(this);
        mTouchDetector = new ForceTouchDetector(
                () -> mListener.onTopicStartPreview(TopicView.this, mTopic),
                () -> mListener.onTopicStopPreview(TopicView.this, mTopic)
        );
    }

    public void setContentListener(HtmlMovementMethod.OnHtmlActionListener listener) {
        mContent.setMovementMethod(new HtmlMovementMethod(listener));
    }

    public void setNodeListener(final NodeListFragment.OnNodeActionListener listener) {
        mNode.setOnClickListener(v -> listener.onNodeOpen(mTopic.getNode()));
    }

    public void setAvatarListener(final AvatarView.OnAvatarActionListener listener) {
        View.OnClickListener tmp = v -> listener.onMemberClick(mTopic.getMember());

        mAvatar.setOnClickListener(tmp);
        mUsername.setOnClickListener(tmp);
    }

    public void fillData(Topic topic) {
        if (!topic.hasInfo()) {
            setVisibility(View.INVISIBLE);
            return;
        }
        setVisibility(View.VISIBLE);
        mTopic = topic;

        updateForRead();

        ViewUtils.setHtmlIntoTextView(mTitle, topic.getTitle(),
                ViewUtils.getDimensionPixelSize(R.dimen.abc_text_size_body_1_material), false);
        mUsername.setText(topic.getMember().getUsername());
        mNode.setText(String.format("› %s", topic.getNode().getTitle()));
        mTime.setText(topic.getReplyTime());
        final int replyCount = topic.getReplyCount();
        if (replyCount > 0) {
            mReplyCount.setVisibility(View.VISIBLE);
            mReplyCount.setText(String.format("%d", replyCount));
        } else {
            mReplyCount.setVisibility(View.INVISIBLE);
        }

        mAvatar.setAvatar(topic.getMember().getAvatar());
        setContent(topic);
    }

    public void updateForRead() {
        if (mTopic.hasRead()) {
            mReplyCount.setAlpha(0.3f);
        } else {
            mReplyCount.setAlpha(1);
        }
    }

    private void setContent(Topic topic) {
        final String content = topic.getContent();
        if (Strings.isNullOrEmpty(content)) {
            mContent.setVisibility(View.GONE);
            return;
        }
        mContent.setVisibility(View.VISIBLE);
        ViewUtils.setHtmlIntoTextView(mContent, content, ViewUtils.getWidthPixels() -
                TOPIC_PICTURE_OTHER_WIDTH, true);
    }

    @Override
    public void onClick(View v) {
        mListener.onTopicOpen(v, mTopic);
        updateForRead();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mTouchDetector.handleEvent(event);
    }

    public interface OnTopicActionListener {
        void onTopicOpen(View view, Topic topic);
        void onTopicStartPreview(View view, Topic topic);
        void onTopicStopPreview(View view, Topic topic);
    }
}
