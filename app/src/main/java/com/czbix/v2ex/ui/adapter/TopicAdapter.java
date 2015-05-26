package com.czbix.v2ex.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.network.ImageLoader;

import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {
    private final OnItemClickListener mListener;
    private List<Topic> mData;

    public TopicAdapter(@NonNull OnItemClickListener listener) {
        mListener = listener;
    }

    public void setDataSource(List<Topic> data) {
        mData = data;

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_topic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Topic topic = mData.get(position);
        holder.fillData(topic);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mTitle;
        public final ImageView mAvatar;
        public final TextView mUsername;
        public final TextView mNode;
        public final TextView mReplyCount;
        public final TextView mTime;

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);

            mAvatar = ((ImageView) view.findViewById(R.id.avatar_img));
            mTitle = ((TextView) view.findViewById(R.id.title_tv));
            mUsername = ((TextView) view.findViewById(R.id.username_tv));
            mNode = ((TextView) view.findViewById(R.id.node_tv));
            mTime = ((TextView) view.findViewById(R.id.time_tv));
            mReplyCount = ((TextView) view.findViewById(R.id.reply_count_tv));
        }

        public void fillData(Topic topic) {
            mTitle.setText(topic.getTitle());
            mUsername.setText(topic.getMember().getUsername());
            mNode.setText(topic.getNode().getTitle());
            mTime.setText(topic.getReplyTime());
            mReplyCount.setText(Integer.toString(topic.getReplyCount()));

            final Avatar avatar = topic.getMember().getAvatar();
            ImageLoader.getInstance().add(mAvatar, avatar.getUrlByDp(mAvatar.getHeight()));
        }

        @Override
        public void onClick(View v) {
            final int position = getAdapterPosition();
            if (v == itemView) {
                mListener.onItemClick(position, mData.get(position));
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Topic topic);
    }
}
