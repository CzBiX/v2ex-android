package com.czbix.v2ex.ui.adapter;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Strings;

import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {
    private final OnItemClickListener mListener;
    private List<Topic> mData;

    public TopicAdapter(@NonNull OnItemClickListener listener) {
        mListener = listener;
        setHasStableIds(true);
    }

    public void setDataSource(List<Topic> data) {
        mData = data;

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_topic, parent, false);
        return new ViewHolder(this, view);
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
        public final TextView mTitle;
        public final ImageView mAvatar;
        public final TextView mUsername;
        public final TextView mNode;
        public final TextView mReplyCount;
        public final TextView mTime;
        public final TextView mContent;

        private final TopicAdapter mAdapter;
        private volatile int mId;

        public ViewHolder(View view) {
            this(null, view);
        }

        public ViewHolder(TopicAdapter adapter, View view) {
            super(view);
            mAdapter = adapter;

            view.setOnClickListener(this);

            mAvatar = ((ImageView) view.findViewById(R.id.avatar_img));
            mTitle = ((TextView) view.findViewById(R.id.title_tv));
            mUsername = ((TextView) view.findViewById(R.id.username_tv));
            mNode = ((TextView) view.findViewById(R.id.node_tv));
            mTime = ((TextView) view.findViewById(R.id.time_tv));
            mReplyCount = ((TextView) view.findViewById(R.id.reply_count_tv));
            mContent = ((TextView) view.findViewById(R.id.content));

            if (adapter == null) {
                // single topic
                mTitle.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                // topic list
                mTitle.setTypeface(Typeface.DEFAULT);
            }
        }

        public void fillData(Topic topic) {
            fillData(topic, false);
        }

        public void fillData(Topic topic, boolean force) {
            if (mId == topic.getId() && !force) {
                return;
            }

            mId = topic.getId();

            mTitle.setText(topic.getTitle());
            mUsername.setText(topic.getMember().getUsername());
            mNode.setText("› " + topic.getNode().getTitle());
            mTime.setText(topic.getReplyTime());
            mReplyCount.setText(Integer.toString(topic.getReplyCount()));

            setContent(topic);
            setAvatarImg(topic);
        }

        private void setContent(Topic topic) {
            final String content = topic.getContent();
            if (Strings.isNullOrEmpty(content)) {
                mContent.setVisibility(View.GONE);
                return;
            }
            mContent.setVisibility(View.VISIBLE);
            ViewUtils.setHtmlIntoTextView(mContent, topic.getContent(),
                    R.dimen.topic_picture_max_width);
            mContent.setMovementMethod(LinkMovementMethod.getInstance());
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
            if (mAdapter == null) {
                return;
            }

            final int position = getAdapterPosition();
            if (v == itemView) {
                mAdapter.mListener.onItemClick(position, v, mAdapter.mData.get(position));
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View v, Topic topic);
    }
}
