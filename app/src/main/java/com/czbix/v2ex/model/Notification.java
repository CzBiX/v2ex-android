package com.czbix.v2ex.model;

import androidx.annotation.IntDef;

import com.czbix.v2ex.db.Member;
import com.google.common.base.Objects;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Notification {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_UNKNOWN, TYPE_THANK_TOPIC, TYPE_REPLY_TOPIC, TYPE_FAV_TOPIC, TYPE_THANK_COMMENT, TYPE_REPLY_COMMENT})
    public  @interface NotificationType {}

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_THANK_TOPIC = 1;
    public static final int TYPE_REPLY_TOPIC = 2;
    public static final int TYPE_FAV_TOPIC = 3;
    public static final int TYPE_THANK_COMMENT = 4;
    public static final int TYPE_REPLY_COMMENT = 5;

    public final Member mMember;
    @NotificationType
    public final int mType;
    public final Topic mTopic;
    public final String mContent;
    public final String mTime;

    public Notification(Member member, Topic topic, @NotificationType int type, String content, String time) {
        mContent = content;
        mMember = member;
        mType = type;
        mTopic = topic;
        mTime = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return Objects.equal(mType, that.mType) &&
                Objects.equal(mMember, that.mMember) &&
                Objects.equal(mTopic, that.mTopic) &&
                Objects.equal(mContent, that.mContent) &&
                Objects.equal(mTime, that.mTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mMember, mType, mTopic, mContent, mTime);
    }

    public static class Builder {
        private Member mMember;
        private Topic mTopic;
        private int mType;
        private String mContent;
        private String mTime;

        public Builder setMember(Member member) {
            mMember = member;
            return this;
        }

        public Builder setTopic(Topic topic) {
            mTopic = topic;
            return this;
        }

        public Builder setType(@NotificationType int type) {
            mType = type;
            return this;
        }

        public Builder setContent(String content) {
            mContent = content;
            return this;
        }

        public Builder setTime(String time) {
            mTime = time;
            return this;
        }

        public Notification createNotification() {
            return new Notification(mMember, mTopic, mType, mContent, mTime);
        }
    }
}
