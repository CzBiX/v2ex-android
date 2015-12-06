package com.czbix.v2ex.ui.loader;

import android.content.Context;

import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.dao.TopicDao;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.TopicWithComments;
import com.czbix.v2ex.network.RequestHelper;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class TopicLoader extends AsyncTaskLoader<TopicWithComments> {
    private final Topic mTopic;
    private int mPage;

    public TopicLoader(Context context, Topic topic) {
        super(context);

        mTopic = topic;
        mPage = 1;
    }

    public void setPage(int page) {
        if (page == mPage) {
            return;
        }

        mPage = page;
        mResult = null;
        onContentChanged();
    }

    @Override
    public TopicWithComments loadInBackgroundWithException() throws ConnectionException, RemoteException {
        final TopicWithComments topicWithComments = RequestHelper.getTopicWithComments(mTopic, mPage);
        if (mPage == 1 && topicWithComments.mComments.size() > 0) {
            final int lastRead = TopicDao.getLastReadReply(mTopic.getId());
            if (lastRead > 7) {
                final List<Integer> commentIds = Lists.transform(topicWithComments.mComments,
                        Comment::getFloor);
                final int index = Collections.binarySearch(commentIds, lastRead);
                // the closest and latest comment of last read pos
                topicWithComments.mLastReadPos = index >= 0 ? index : -index - 2;
            }
        }

        return topicWithComments;
    }
}
