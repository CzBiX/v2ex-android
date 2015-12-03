package com.czbix.v2ex.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.fragment.NodeListFragment.OnNodeActionListener;
import com.czbix.v2ex.ui.widget.AvatarView;
import com.czbix.v2ex.ui.widget.HtmlMovementMethod;
import com.czbix.v2ex.ui.widget.HtmlMovementMethod.OnHtmlActionListener;
import com.czbix.v2ex.ui.widget.TopicView;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Strings;

import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {
    private final TopicView.OnTopicActionListener mListener;
    private List<Topic> mData;

    public TopicAdapter(@NonNull TopicView.OnTopicActionListener listener) {
        mListener = listener;
        setHasStableIds(true);
    }

    public void setDataSource(List<Topic> data) {
        mData = data;

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new TopicView(parent.getContext()), mListener);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TopicView mView;

        public ViewHolder(TopicView view, TopicView.OnTopicActionListener listener) {
            super(view);
            view.setListener(listener);
            mView = view;
        }

        public void fillData(Topic topic) {
            mView.fillData(topic);
        }
    }
}
