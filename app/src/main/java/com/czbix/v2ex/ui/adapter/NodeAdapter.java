package com.czbix.v2ex.ui.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.ui.fragment.NodeListFragment;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;

public class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.ViewHolder> {
    private final NodeListFragment.OnNodeActionListener mListener;
    private List<Node> mData;
    private List<Node> mAllData;

    public NodeAdapter(NodeListFragment.OnNodeActionListener listener) {
        mListener = listener;

        setHasStableIds(true);
    }

    public void setDataSource(List<Node> data) {
        mData = data;
        mAllData = mData;
        notifyDataSetChanged();
    }

    public void filterText(CharSequence query) {
        if (TextUtils.isEmpty(query)) {
            mData = mAllData;
            notifyDataSetChanged();
            return;
        }

        List<Node> result = Lists.newArrayList();
        for (Node node : mAllData) {
            if (node.getName().contains(query) ||
                    node.getTitle().contains(query) ||
                    (node.getTitleAlternative() != null && node.getTitleAlternative().contains(query)))
                result.add(node);
        }

        mData = result;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_node,
                parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Node node = mData.get(position);
        holder.fillData(node);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getId();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTitle;
        private final TextView mAlertTitle;
        private final ImageView mAvatar;

        public ViewHolder(View view) {
            super(view);

            mTitle = ((TextView) view.findViewById(R.id.title));
            mAlertTitle = (TextView) view.findViewById(R.id.alertTitle);
            mAvatar = (ImageView) view.findViewById(R.id.avatar_img);

            view.setOnClickListener(this);
        }

        public void fillData(Node node) {
            mTitle.setText(node.getTitle());
            final String alternative = node.getTitleAlternative();
            if (Strings.isNullOrEmpty(alternative)) {
                mAlertTitle.setVisibility(View.INVISIBLE);
            } else {
                mAlertTitle.setVisibility(View.VISIBLE);
                mAlertTitle.setText(alternative);
            }

            setAvatarImg(node);
        }

        @Override
        public void onClick(View v) {
            final int position = getAdapterPosition();
            ViewUtils.hideInputMethod(mTitle);
            mListener.onNodeOpen(mData.get(position));
        }

        public void setAvatarImg(Node avatarImg) {
            final Avatar avatar = avatarImg.getAvatar();
            if (avatar == null) {
                return;
            }

            final float dimen = mAvatar.getResources().getDimension(R.dimen.node_avatar_size);
            Glide.with(mAvatar.getContext()).load(avatar.getUrlByDp(dimen)).crossFade()
                    .into(mAvatar);
        }
    }
}
