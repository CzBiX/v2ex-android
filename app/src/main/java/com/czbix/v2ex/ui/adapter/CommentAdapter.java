package com.czbix.v2ex.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.widget.CommentView;
import com.czbix.v2ex.ui.widget.CommentView.OnCommentActionListener;
import com.czbix.v2ex.ui.widget.TopicView;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.BaseViewHolder> {
    private static final int TYPE_TOPIC = 0;
    private static final int TYPE_COMMENT = 1;

    private final OnCommentActionListener mListener;
    private Topic mTopic;
    private List<Comment> mCommentList;

    public CommentAdapter(OnCommentActionListener listener) {
        mListener = listener;
        setHasStableIds(true);
    }

    public void setTopic(Topic topic) {
        if (mTopic != null && mTopic.hasInfo()) {
            notifyItemChanged(0);
        } else {
            notifyItemInserted(0);
        }
        mTopic = topic;
    }

    public void setDataSource(List<Comment> comments) {
        mCommentList = comments;
        notifyDataSetChanged();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder viewHolder = null;
        Context context = parent.getContext();
        switch (viewType) {
            case TYPE_TOPIC:
                viewHolder = new TopicViewHolder(new TopicView(context));
                break;
            case TYPE_COMMENT:
                viewHolder = new CommentViewHolder(new CommentView(context), mListener);
        }

        return viewHolder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (position-- == 0) {
            holder.fillData(mTopic);
        } else {
            Comment comment = mCommentList.get(position);
            holder.fillData(comment);
        }
    }

    @Override
    public long getItemId(int position) {
        if (position-- == 0) {
            return mTopic.getId();
        }
        return mCommentList.get(position).getId();
    }

    @Override
    public int getItemCount() {
        int commentNum = mCommentList == null ? 0 : mCommentList.size();
        int topicNum = mTopic.hasInfo() ? 1 : 0;
        return topicNum + commentNum;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_TOPIC : TYPE_COMMENT;
    }

    static abstract class BaseViewHolder<D, V extends View> extends RecyclerView.ViewHolder {
        protected final V mView;

        public BaseViewHolder(V view) {
            super(view);
            mView = view;
        }

        public abstract void fillData(D data);
    }

    static class CommentViewHolder extends BaseViewHolder<Comment, CommentView> {

        public CommentViewHolder(CommentView view, OnCommentActionListener listener) {
            super(view);
            view.setListener(listener);
        }

        @Override
        public void fillData(Comment comment) {
            mView.fillData(comment, getAdapterPosition());
        }
    }

    static class TopicViewHolder extends BaseViewHolder<Topic, TopicView> {
        public TopicViewHolder(TopicView view) {
            super(view);
        }

        @Override
        public void fillData(Topic data) {
            mView.fillData(data);
        }
    }
}
