package com.czbix.v2ex.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.fragment.NodeListFragment.OnNodeActionListener;
import com.czbix.v2ex.ui.widget.HtmlMovementMethod;
import com.czbix.v2ex.ui.widget.HtmlMovementMethod.OnHtmlActionListener;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Strings;

import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {
    private final OnTopicActionListener mListener;
    private List<Topic> mData;

    public TopicAdapter(@NonNull OnTopicActionListener listener) {
        mListener = listener;
        setHasStableIds(true);
    }

    public void setDataSource(List<Topic> data) {
        mData = data;

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_topic, parent, false);
        return new ViewHolder(mListener, view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Topic topic = mData.get(position);
        holder.fillData(topic);
    }

    @Override
    public long getItemId(int position) {
        return mData == null ? RecyclerView.NO_ID : mData.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private static final int TOPIC_PICTURE_OTHER_WIDTH = ViewUtils.getDimensionPixelSize(R.dimen.topic_picture_other_width);

        public final TextView mTitle;
        public final ImageView mAvatar;
        public final TextView mUsername;
        public final TextView mNode;
        public final TextView mReplyCount;
        public final TextView mTime;
        public final TextView mContent;

        private final OnTopicActionListener mListener;
        private Topic mTopic;

        public ViewHolder(View view) {
            this(null, view);
        }

        public ViewHolder(OnTopicActionListener listener, View view) {
            super(view);
            mListener = listener;

            view.setOnClickListener(this);

            mAvatar = ((ImageView) view.findViewById(R.id.avatar_img));
            mTitle = ((TextView) view.findViewById(R.id.title_tv));
            mUsername = ((TextView) view.findViewById(R.id.username_tv));
            mNode = ((TextView) view.findViewById(R.id.node_tv));
            mTime = ((TextView) view.findViewById(R.id.time_tv));
            mReplyCount = ((TextView) view.findViewById(R.id.reply_count_tv));
            mContent = ((TextView) view.findViewById(R.id.content));
        }

        public void setContentListener(OnHtmlActionListener listener) {
            mContent.setMovementMethod(new HtmlMovementMethod(listener));
        }

        public void setNodeListener(final OnNodeActionListener listener) {
            mNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onNodeOpen(mTopic.getNode());
                }
            });
        }

        public void setMemberListener(final OnMemberActionListener listener) {
            View.OnClickListener tmp = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onMemberClick(mTopic.getMember());
                }
            };

            mAvatar.setOnClickListener(tmp);
            mUsername.setOnClickListener(tmp);
        }

        public void fillData(Topic topic) {
            if (topic.equals(mTopic)) {
                return;
            }
            if (!topic.hasInfo()) {
                itemView.setVisibility(View.INVISIBLE);
                return;
            }
            itemView.setVisibility(View.VISIBLE);
            mTopic = topic;

            updateForRead();

            ViewUtils.setHtmlIntoTextView(mTitle, topic.getTitle(),
                    ViewUtils.getDimensionPixelSize(R.dimen.abc_text_size_body_1_material));
            mUsername.setText(topic.getMember().getUsername());
            mNode.setText("› " + topic.getNode().getTitle());
            mTime.setText(topic.getReplyTime());
            final int replyCount = topic.getReplyCount();
            if (replyCount > 0) {
                mReplyCount.setVisibility(View.VISIBLE);
                mReplyCount.setText(Integer.toString(replyCount));
            } else {
                mReplyCount.setVisibility(View.INVISIBLE);
            }

            setContent(topic);
            setAvatarImg(topic);
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
                    TOPIC_PICTURE_OTHER_WIDTH);
        }

        private void setAvatarImg(Topic topic) {
            final float dimen = mAvatar.getResources().getDimension(R.dimen.topic_avatar_size);
            final String url = topic.getMember().getAvatar().getUrlByDp(dimen);
            Glide.with(mAvatar.getContext()).load(url)
                    .placeholder(R.drawable.avatar_default).crossFade()
                    .into(mAvatar);
        }

        @Override
        public void onClick(View v) {
            if (mListener == null) {
                return;
            }

            if (mListener.onTopicOpen(v, mTopic)) {
                updateForRead();
            }
        }
    }

    public interface OnTopicActionListener {
        /**
         * @return should refresh data
         */
        boolean onTopicOpen(View view, Topic topic);
    }

    public interface OnMemberActionListener {
        void onMemberClick(Member member);
    }
}
