package com.czbix.v2ex.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.czbix.v2ex.R;
import com.czbix.v2ex.ViewerProvider;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.network.GlideApp;
import com.czbix.v2ex.util.ViewUtils;

public class CommentView extends ConstraintLayout implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener, HtmlMovementMethod.OnHtmlActionListener {

    private final TextView mContent;
    public final AvatarView mAvatar;
    private final TextView mUsername;
    private final TextView mReplyTime;
    private final TextView mFloor;
    private final TextView mThanks;
    private final TextView mAuthor;
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

        inflate(context, R.layout.layout_comment, this);

        mAvatar = findViewById(R.id.avatar_img);
        mContent = findViewById(R.id.content);
        mUsername = findViewById(R.id.username_tv);
        mReplyTime = findViewById(R.id.time_tv);
        mFloor = findViewById(R.id.floor);
        mThanks = findViewById(R.id.thanks);
        mAuthor = findViewById(R.id.tv_author);

        ViewUtils.setSpannableFactory(mContent);

        mAvatar.setOnClickListener(this);
        mUsername.setOnClickListener(this);

        setOnCreateContextMenuListener(this);
        mContent.setMovementMethod(new HtmlMovementMethod(this));
    }

    public void setListener(OnCommentActionListener listener) {
        mListener = listener;
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void fillData(RequestManager glide, Comment comment, boolean isAuthor, int position) {
        if (comment.equals(mComment)) {
            return;
        }
        mComment = comment;
        mPos = position;

        ViewUtils.setHtmlIntoTextView(mContent, comment.getContent(), 0, false);
        appendThanks(comment);

        mUsername.setText(comment.getMember().getUsername());
        mReplyTime.setText(comment.getReplyTime());

        mFloor.setText(Integer.toString(comment.getFloor()));

        mAuthor.setVisibility(isAuthor ? View.VISIBLE : View.GONE);

        mAvatar.setAvatar(glide, comment.getMember().getAvatar());
    }

    public void clear(RequestManager glide) {
        glide.clear(mAvatar);
        mContent.setText(null);
    }

    private void appendThanks(Comment comment) {
        if (comment.getThanks() <= 0) {
            mThanks.setVisibility(View.INVISIBLE);
            return;
        }

        final String text = "+" + comment.getThanks();
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
        ViewerProvider.Companion.viewImage(getContext(), GlideApp.with(this), source);
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
