package com.czbix.v2ex.ui.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.czbix.v2ex.R;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.ui.widget.HtmlMovementMethod;
import com.czbix.v2ex.ui.widget.HtmlMovementMethod.OnHtmlActionListener;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Preconditions;

import java.util.List;

public class CommentAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final OnCommentActionListener mListener;
    private List<Comment> mCommentList;

    public CommentAdapter(Context context, OnCommentActionListener listener) {
        mInflater = LayoutInflater.from(context);
        mListener = listener;
    }

    public void setDataSource(List<Comment> comments) {
        mCommentList = comments;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCommentList == null ? 0 : mCommentList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCommentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mCommentList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.view_comment, parent , false);
            viewHolder = new ViewHolder(convertView, mListener);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = ((ViewHolder) convertView.getTag());
            Preconditions.checkNotNull(viewHolder);
        }

        final Comment comment = mCommentList.get(position);
        viewHolder.fillData(comment);

        return convertView;
    }

    private static class ViewHolder implements View.OnCreateContextMenuListener, View.OnClickListener, MenuItem.OnMenuItemClickListener, OnHtmlActionListener {
        private final TextView mContent;
        private final ImageView mAvatar;
        private final TextView mUsername;
        private final TextView mReplyTime;
        private final TextView mFloor;
        private final TextView mThanks;
        private final OnCommentActionListener mListener;
        private Comment mComment;

        public ViewHolder(View view, OnCommentActionListener listener) {
            mAvatar = ((ImageView) view.findViewById(R.id.avatar_img));
            mContent = (TextView) view.findViewById(R.id.content);
            mUsername = (TextView) view.findViewById(R.id.username_tv);
            mReplyTime = ((TextView) view.findViewById(R.id.time_tv));
            mFloor = ((TextView) view.findViewById(R.id.floor));
            mThanks = ((TextView) view.findViewById(R.id.thanks));

            mListener = listener;
            view.setOnClickListener(this);
            mAvatar.setOnClickListener(this);
            mUsername.setOnClickListener(this);
            mContent.setOnClickListener(this);

            view.setOnCreateContextMenuListener(this);
            mContent.setMovementMethod(new HtmlMovementMethod(this));
        }

        public void fillData(Comment comment) {
            if (comment.equals(mComment)) {
                return;
            }
            mComment = comment;

            ViewUtils.setHtmlIntoTextViewWithRes(mContent, comment.getContent(),
                    R.dimen.comment_picture_max_width);
            appendThanks(comment);

            mUsername.setText(comment.getMember().getUsername());
            mReplyTime.setText(comment.getReplyTime());
            mFloor.setText(Integer.toString(comment.getFloor()));

            setAvatarImg(comment);
        }

        private void appendThanks(Comment comment) {
            if (comment.getThanks() <= 0) {
                mThanks.setVisibility(View.INVISIBLE);
                return;
            }

            final String text = "+" + Integer.toString(comment.getThanks());
            if (comment.isThanked()) {
                final ForegroundColorSpan span = new ForegroundColorSpan(
                        mThanks.getContext().getResources().getColor(R.color.highlight_green));
                final SpannableString string = new SpannableString(text);
                string.setSpan(span, 0, text.length(), 0);

                mThanks.setText(string);
            } else {
                mThanks.setText(text);
            }
            mThanks.setVisibility(View.VISIBLE);
        }

        public void setAvatarImg(Comment comment) {
            final float dimen = mAvatar.getResources().getDimension(R.dimen.comment_avatar_size);
            final String url = comment.getMember().getAvatar().getUrlByDp(dimen);
            Glide.with(mAvatar.getContext()).load(url)
                    .placeholder(R.drawable.avatar_default).crossFade()
                    .into(mAvatar);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
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
                return;
            }

            if (UserState.getInstance().isGuest()) {
                // anonymous can't do anything
                return;
            }

            final String username = UserState.getInstance().getUsername();
            if (mComment.getMember().getUsername().equals(username)) {
                // can't do action on comment by myself
                return;
            }
            v.showContextMenu();
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
                    mListener.onCommentCopy(mComment);
                    return true;
            }
            return false;
        }

        @Override
        public void onUrlClick(String url) {
            mListener.onCommentUrlClick(url);
        }

        @Override
        public void onImageClick(String source) {
            mListener.onCommentUrlClick(source);
        }
    }

    public interface OnCommentActionListener {
        void onMemberClick(Member member);
        void onCommentThank(Comment comment);
        void onCommentReply(Comment comment);
        void onCommentIgnore(Comment comment);
        void onCommentCopy(Comment comment);
        void onCommentUrlClick(String url);
    }
}
