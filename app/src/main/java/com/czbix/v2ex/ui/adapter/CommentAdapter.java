package com.czbix.v2ex.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.Postscript;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.fragment.NodeListFragment.OnNodeActionListener;
import com.czbix.v2ex.ui.widget.AvatarView.OnAvatarActionListener;
import com.czbix.v2ex.ui.widget.CommentView;
import com.czbix.v2ex.ui.widget.CommentView.OnCommentActionListener;
import com.czbix.v2ex.ui.widget.HtmlMovementMethod;
import com.czbix.v2ex.ui.widget.HtmlMovementMethod.OnHtmlActionListener;
import com.czbix.v2ex.ui.widget.TopicView;
import com.czbix.v2ex.util.ViewUtils;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.BaseViewHolder> {
    private static final int TYPE_TOPIC = 0;
    private static final int TYPE_COMMENT = 1;

    private final OnCommentActionListener mCommentListener;
    private final OnHtmlActionListener mContentListener;
    private final OnNodeActionListener mNodeListener;
    private final OnAvatarActionListener mAvatarListener;
    private Topic mTopic;
    private List<Comment> mCommentList;

    public CommentAdapter(OnCommentActionListener commentListener,
                          OnHtmlActionListener contentListener,
                          OnNodeActionListener nodeListener,
                          OnAvatarActionListener avatarListener) {
        mCommentListener = commentListener;
        mContentListener = contentListener;
        mNodeListener = nodeListener;
        mAvatarListener = avatarListener;
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
                viewHolder = TopicViewHolder.makeHolder(parent, mContentListener, mNodeListener, mAvatarListener);
                break;
            case TYPE_COMMENT:
                viewHolder = new CommentViewHolder(new CommentView(context), mCommentListener);
        }

        return viewHolder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (position-- == 0) {
            ((TopicViewHolder) holder).fillData(mTopic);
        } else {
            Comment comment = mCommentList.get(position);
            ((CommentViewHolder) holder).fillData(comment);
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

    static abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(View view) {
            super(view);
        }
    }

    static class CommentViewHolder extends BaseViewHolder {

        public CommentViewHolder(CommentView view, OnCommentActionListener listener) {
            super(view);
            view.setListener(listener);
        }

        public void fillData(Comment comment) {
            ((CommentView) itemView).fillData(comment, getAdapterPosition());
        }
    }

    static class TopicViewHolder extends BaseViewHolder {
        private static final int TOPIC_PICTURE_OTHER_WIDTH = ViewUtils.getDimensionPixelSize(R.dimen.topic_picture_other_width);

        private final LinearLayout mTopicLayout;
        private final TopicView mTopicView;
        private final OnHtmlActionListener mContentListener;

        private TopicViewHolder(LinearLayout layout, TopicView view, OnHtmlActionListener contentListener) {
            super(layout);
            mTopicLayout = layout;
            mTopicView = view;
            mContentListener = contentListener;
        }

        public static TopicViewHolder makeHolder(ViewGroup parent,
                                                 OnHtmlActionListener contentListener,
                                                 OnNodeActionListener nodeListener,
                                                 OnAvatarActionListener avatarListener) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.view_comment_topic, parent, false);
            TopicView view = (TopicView) layout.findViewById(R.id.topic);

            view.setContentListener(contentListener);
            view.setNodeListener(nodeListener);
            view.setAvatarListener(avatarListener);

            return new TopicViewHolder(layout, view, contentListener);
        }

        public void fillData(Topic data) {
            mTopicView.fillData(data);
            fillPostscript(data.getPostscripts());
        }

        private void fillPostscript(List<Postscript> postscripts) {
            if (postscripts == null) {
                return;
            }

            final int childCount = mTopicLayout.getChildCount();
            if (childCount > 1) {
                mTopicLayout.removeViews(1, childCount - 1);
            }

            Context context = mTopicLayout.getContext();
            final LayoutInflater inflater = LayoutInflater.from(context);

            for (int i = 0, size = postscripts.size(); i < size; i++) {
                Postscript postscript = postscripts.get(i);

                final View view = inflater.inflate(R.layout.view_postscript, mTopicLayout, false);
                ((TextView) view.findViewById(R.id.title)).setText(context.getString(R.string.title_postscript, i + 1));
                ((TextView) view.findViewById(R.id.time)).setText(postscript.mTime);
                final TextView contentView = (TextView) view.findViewById(R.id.content);
                ViewUtils.setHtmlIntoTextView(contentView, postscript.mContent,
                        ViewUtils.getWidthPixels() - TOPIC_PICTURE_OTHER_WIDTH, true);
                contentView.setMovementMethod(new HtmlMovementMethod(mContentListener));
                mTopicLayout.addView(view);
            }
        }
    }
}
