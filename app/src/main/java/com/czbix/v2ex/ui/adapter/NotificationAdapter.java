package com.czbix.v2ex.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Notification;
import com.czbix.v2ex.ui.widget.AvatarView;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Strings;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private final OnNotificationActionListener mListener;
    private List<Notification> mData;

    public NotificationAdapter(@NonNull OnNotificationActionListener listener) {
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_notification, parent, false);
        return new ViewHolder(mListener, view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Notification notification = mData.get(position);
        holder.fillData(notification);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setDataSource(List<Notification> data) {
        mData = data;

        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final OnNotificationActionListener mListener;
        private final AvatarView mAvatar;
        private final TextView mTitle;
        private final TextView mUsername;
        private final TextView mTime;
        private final TextView mAction;
        private final TextView mContent;
        private Notification mNotification;

        public ViewHolder(OnNotificationActionListener listener, View view) {
            super(view);
            mListener = listener;

            view.setOnClickListener(this);

            mAvatar = (AvatarView) view.findViewById(R.id.avatar_img);
            mTitle = (TextView) view.findViewById(R.id.title_tv);
            mUsername = (TextView) view.findViewById(R.id.username_tv);
            mTime = (TextView) view.findViewById(R.id.time_tv);
            mAction = (TextView) view.findViewById(R.id.action);
            mContent = ((TextView) view.findViewById(R.id.content));
        }

        public void fillData(Notification notification) {
            if (notification.equals(mNotification)){
                return;
            }

            mNotification = notification;

            mTitle.setText(notification.mTopic.getTitle());
            mUsername.setText(notification.mMember.getUsername());
            mAction.setText(getTypeText(notification.mType));
            mTime.setText(notification.mTime);

            mAvatar.setAvatar(Glide.with(mAvatar), mNotification.mMember.getAvatar());
            setContent(notification);
        }

        private void setContent(Notification notification) {
            final String content = notification.mContent;
            if (Strings.isNullOrEmpty(content)) {
                mContent.setVisibility(View.GONE);
                return;
            }
            mContent.setVisibility(View.VISIBLE);
            ViewUtils.setHtmlIntoTextView(mContent, content, 0, false);
        }

        private String getTypeText(@Notification.NotificationType int type) {
            int resId;
            switch (type) {
                case Notification.TYPE_REPLY_TOPIC:
                    resId = R.string.notification_reply_topic;
                    break;
                case Notification.TYPE_THANK_TOPIC:
                    resId = R.string.notification_thank_topic;
                    break;
                case Notification.TYPE_FAV_TOPIC:
                    resId = R.string.notification_fav_topic;
                    break;
                case Notification.TYPE_REPLY_COMMENT:
                    resId = R.string.notification_reply_comment;
                    break;
                case Notification.TYPE_THANK_COMMENT:
                    resId = R.string.notification_thank_comment;
                    break;
                case Notification.TYPE_UNKNOWN:
                default:
                    resId = R.string.notification_unknown;
                    break;
            }

            return mAction.getContext().getString(resId);
        }

        @Override
        public void onClick(View v) {
            mListener.onNotificationOpen(mNotification);
        }
    }

    public interface OnNotificationActionListener {
        void onNotificationOpen(Notification notification);
    }
}
