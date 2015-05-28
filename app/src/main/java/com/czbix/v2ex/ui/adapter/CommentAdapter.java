package com.czbix.v2ex.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.network.ImageLoader;

import java.util.List;

public class CommentAdapter extends ArrayAdapter<Comment> {
    private final LayoutInflater mInflater;

    public CommentAdapter(Context context) {
        super(context, 0);

        mInflater = LayoutInflater.from(context);
    }

    public void setDataSource(List<Comment> comments) {
        clear();
        if (comments != null) {
            addAll(comments);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.view_comment, parent , false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = ((ViewHolder) convertView.getTag());
        }

        final Comment comment = getItem(position);
        viewHolder.fillData(comment);

        return convertView;
    }

    private static class ViewHolder implements ImageLoader.Callback {
        private final TextView mContent;
        private final ImageView mAvatar;
        private final TextView mUsername;
        private final TextView mReplyTime;

        private volatile int mId;

        public ViewHolder(View view) {
            mAvatar = ((ImageView) view.findViewById(R.id.avatar_img));
            mContent = (TextView) view.findViewById(R.id.content);
            mUsername = (TextView) view.findViewById(R.id.username_tv);
            mReplyTime = ((TextView) view.findViewById(R.id.time_tv));
        }

        public void fillData(Comment comment) {
            if (mId == comment.getId()) {
                return;
            }

            mId = comment.getId();

            mContent.setText(Html.fromHtml(comment.getContent()));
            mUsername.setText(comment.getMember().getUsername());
            mReplyTime.setText(comment.getReplyTime());

            setAvatarImg(comment);
        }

        public void setAvatarImg(Comment comment) {
            final String url = comment.getMember().getAvatar().getUrlByDp(32);
            mAvatar.setImageResource(R.drawable.avatar_default);
            ImageLoader.getInstance().add(mId, mAvatar, url, this);
        }

        @Override
        public boolean isTaskIdValid(int taskId) {
            return mId == taskId;
        }
    }
}
