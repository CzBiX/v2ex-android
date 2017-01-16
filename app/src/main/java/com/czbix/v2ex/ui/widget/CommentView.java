package com.czbix.v2ex.ui.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.czbix.v2ex.R;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.util.ViewUtils;

public class CommentView extends FrameLayout implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener, HtmlMovementMethod.OnHtmlActionListener {
    private static final int COMMENT_PICTURE_OTHER_WIDTH =
            ViewUtils.getDimensionPixelSize(R.dimen.comment_picture_other_width);

    private final TextView mContent;
    private final AvatarView mAvatar;
    private final TextView mUsername;
    private final TextView mReplyTime;
    private final TextView mFloor;
    private final TextView mThanks;
    private OnCommentActionListener mListener;
    private Comment mComment;
    private int mPos;

    public CommentView(Context context) {
        this(context, null);
    }

    public CommentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_comment, this);

        mAvatar = (AvatarView) findViewById(R.id.avatar_img);
        mContent = (TextView) findViewById(R.id.content);
        mUsername = (TextView) findViewById(R.id.username_tv);
        mReplyTime = (TextView) findViewById(R.id.time_tv);
        mFloor = (TextView) findViewById(R.id.floor);
        mThanks = (TextView) findViewById(R.id.thanks);

        mAvatar.setOnClickListener(this);
        mUsername.setOnClickListener(this);

        setOnCreateContextMenuListener(this);
        mContent.setMovementMethod(new HtmlMovementMethod(this));
    }

    public void setListener(OnCommentActionListener listener) {
        mListener = listener;
    }

    public void fillData(Comment comment, int position) {
        if (comment.equals(mComment)) {
            return;
        }
        mComment = comment;
        mPos = position;

        ViewUtils.setHtmlIntoTextView(mContent, comment.getContent(), ViewUtils.getWidthPixels() -
                COMMENT_PICTURE_OTHER_WIDTH, false);
        appendThanks(comment);

        mUsername.setText(comment.getMember().getUsername());
        mReplyTime.setText(comment.getReplyTime());
        mFloor.setText(Integer.toString(comment.getFloor()));

        mAvatar.setAvatar(comment.getMember().getAvatar());
    }

    private void appendThanks(Comment comment) {
        if (comment.getThanks() <= 0) {
            mThanks.setVisibility(View.INVISIBLE);
            return;
        }

        final String text = "+" + Integer.toString(comment.getThanks());
        if (comment.isThanked()) {
            final ForegroundColorSpan span = new ForegroundColorSpan(
                    ContextCompat.getColor(mThanks.getContext(), R.color.highlight_green));
            final SpannableString string = new SpannableString(text);
            string.setSpan(span, 0, text.length(), 0);

            mThanks.setText(string);
        } else {
            mThanks.setText(text);
        }
        mThanks.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (!UserState.INSTANCE.isLoggedIn()) {
            // anonymous can't do anything
            return;
        }

        final String username = UserState.INSTANCE.getUsername();
        if (mComment.getMember().getUsername().equals(username)) {
            // can't do action on comment by myself
            return;
        }

        final MenuInflater inflater = new MenuInflater(mContent.getContext());
        inflater.inflate(R.menu.menu_comment, menu);
        for (int i = 0; i < menu.size(); i++) {
            final MenuItem item = menu.getItem(i);
            item.setOnMenuItemClickListener(this);
            if (item.getItemId() == R.id.action_thank) {
                item.setEnabled(!mComment.isThanked());
            }
        }

        menu.setHeaderTitle(R.string.menu_title_comment);
    }

    @Override
    public void onClick(View v) {
        if (v == mAvatar || v == mUsername) {
            mListener.onMemberClick(mComment.getMember());
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_thank:
                mListener.onCommentThank(mComment);
                return true;
            case R.id.action_reply:
                mListener.onCommentReply(mComment);
                return true;
            case R.id.action_ignore:
                mListener.onCommentIgnore(mComment);
                return true;
            case R.id.action_copy:
                mListener.onCommentCopy(mComment, mContent.getText().toString());
                return true;
        }
        return false;
    }

    @Override
    public void onUrlClick(String url) {
        mListener.onCommentUrlClick(url, mPos);
    }

    @Override
    public void onImageClick(String source) {
        mListener.onCommentUrlClick(source, mPos);
    }

    public interface OnCommentActionListener {
        void onMemberClick(Member member);
        void onCommentThank(Comment comment);
        void onCommentReply(Comment comment);
        void onCommentIgnore(Comment comment);
        void onCommentCopy(Comment comment, String content);
        void onCommentUrlClick(String url, int pos);
    }
}
