package com.czbix.v2ex.ui.fragment;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.TopicWithComments;
import com.czbix.v2ex.ui.MainActivity;
import com.czbix.v2ex.ui.adapter.CommentAdapter;
import com.czbix.v2ex.ui.adapter.TopicAdapter;
import com.czbix.v2ex.ui.loader.TopicLoader;
import com.czbix.v2ex.ui.widget.MultiSwipeRefreshLayout;
import com.google.common.base.Preconditions;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TopicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopicFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<TopicWithComments> {
    private static final String ARG_TOPIC = "topic";

    private Topic mTopic;
    private MultiSwipeRefreshLayout mLayout;
    private ListView mCommentsView;
    private TopicAdapter.ViewHolder mTopicHolder;
    private CommentAdapter mCommentAdapter;
    private View mTopicView;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TopicFragment.
     */
    public static TopicFragment newInstance(Topic topic) {
        TopicFragment fragment = new TopicFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TOPIC, topic);
        fragment.setArguments(args);
        return fragment;
    }

    public TopicFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTopic = getArguments().getParcelable(ARG_TOPIC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLayout = (MultiSwipeRefreshLayout) inflater.inflate(R.layout.fragment_topic, container, false);
        mLayout.setOnRefreshListener(this);

        mCommentsView = ((ListView) mLayout.findViewById(R.id.comments));

        mTopicView = inflater.inflate(R.layout.view_topic, mCommentsView, false);
        mTopicView.setBackgroundColor(Color.WHITE);
        mCommentsView.addHeaderView(mTopicView);

        mTopicHolder = new TopicAdapter.ViewHolder(mTopicView);
        mTopicHolder.fillData(mTopic);

        mCommentAdapter = new CommentAdapter(getActivity());
        mCommentsView.setAdapter(mCommentAdapter);

        return mLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity activity = (MainActivity) getActivity();
        activity.setTitle(null);
        final ActionBar actionBar = activity.getSupportActionBar();
        Preconditions.checkNotNull(actionBar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mLayout.setRefreshing(true);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onRefresh() {
        final Loader<Object> loader = getLoaderManager().getLoader(0);
        if (loader == null) {
            return;
        }
        loader.forceLoad();
    }

    @Override
    public Loader<TopicWithComments> onCreateLoader(int id, Bundle args) {
        return new TopicLoader(getActivity(), mTopic);
    }

    @Override
    public void onLoadFinished(Loader<TopicWithComments> loader, TopicWithComments data) {
        mTopicHolder.fillData(data.mTopic, true);
        mCommentAdapter.setDataSource(data.mComments);
        mLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<TopicWithComments> loader) {
        mCommentAdapter.setDataSource(null);
    }
}
