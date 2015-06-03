package com.czbix.v2ex.ui.adapter;

import android.content.Context;
import android.text.method.LinkMovementMethod;
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
import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Preconditions;

import java.util.List;

public class CommentAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final View mHeaderView;
    private final OnCommentActionListener mListener;
    private List<Comment> mCommentList;

    public CommentAdapter(Context context, View headerView, OnCommentActionListener listener) {
        mInflater = LayoutInflater.from(context);
        mHeaderView = headerView;
        mListener = listener;
    }

    public void setDataSource(List<Comment> comments) {
        mCommentList = comments;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mCommentList == null) {
            return 1;
        }

        return mCommentList.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position == 0) {
            return mHeaderView;
        }

        return position - 1;
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return 0;
        }
        position--;
        if (mCommentList == null) {
            return -1;
        }

        return mCommentList.get(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return IGNORE_ITEM_VIEW_TYPE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            return mHeaderView;
        }
        position--;

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

    private static class ViewHolder implements View.OnCreateContextMenuListener, View.OnClickListener, MenuItem.OnMenuItemClickListener {
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
            mContent.setOnClickListener(this);
            view.setOnCreateContextMenuListener(this);
        }

        public void fillData(Comment comment) {
            if (mComment != null && mComment.getId() == comment.getId()) {
                return;
            }
            mComment = comment;

            ViewUtils.setHtmlIntoTextView(mContent, comment.getContent(),
                    R.dimen.comment_picture_max_width);
            mContent.setMovementMethod(LinkMovementMethod.getInstance());
            appendThanks(comment);

            mUsername.setText(comment.getMember().getUsername());
            mReplyTime.setText(comment.getReplyTime());
            mFloor.setText(Integer.toString(comment.getFloor()));

            setAvatarImg(comment);
        }

        private void appendThanks(Comment comment) {
            if (comment.getThanks() > 0) {
                final String text = "+" + Integer.toString(comment.getThanks());
                mThanks.setText(text);
                mThanks.setVisibility(View.VISIBLE);
            } else {
                mThanks.setVisibility(View.INVISIBLE);
            }
        }

        public void setAvatarImg(Comment comment) {
            final float dimen = mAvatar.getResources().getDimension(R.dimen.avatar_size);
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
                menu.getItem(i).setOnMenuItemClickListener(this);
            }

            menu.setHeaderTitle(R.string.menu_title_comment);
        }

        @Override
        public void onClick(View v) {
            if (mComment.getMember().getUsername().equals(AppCtx.getInstance().getUsername())) {
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
    }

    public interface OnCommentActionListener {
        void onCommentThank(Comment comment);
        void onCommentReply(Comment comment);
        void onCommentIgnore(Comment comment);
        void onCommentCopy(Comment comment);
    }
}
