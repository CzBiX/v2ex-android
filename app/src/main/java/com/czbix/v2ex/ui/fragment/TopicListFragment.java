package com.czbix.v2ex.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.dao.NodeDao;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.model.Page;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.MainActivity;
import com.czbix.v2ex.ui.TopicActivity;
import com.czbix.v2ex.ui.TopicEditActivity;
import com.czbix.v2ex.ui.adapter.TopicAdapter;
import com.czbix.v2ex.ui.loader.AsyncTaskLoader.LoaderResult;
import com.czbix.v2ex.ui.loader.TopicListLoader;
import com.czbix.v2ex.ui.presenter.FloatTopicPresenter;
import com.czbix.v2ex.ui.widget.DividerItemDecoration;
import com.czbix.v2ex.ui.widget.TopicView.OnTopicActionListener;
import com.czbix.v2ex.util.ExceptionUtils;
import com.czbix.v2ex.util.LogUtils;
import com.czbix.v2ex.util.TrackerUtils;

import java.util.List;

public class TopicListFragment extends Fragment implements LoaderCallbacks<LoaderResult<List<Topic>>>,
        SwipeRefreshLayout.OnRefreshListener, OnTopicActionListener {
    private static final String TAG = TopicListFragment.class.getSimpleName();
    private static final String ARG_PAGE = "page";

    private Page mPage;

    private TopicAdapter mAdapter;
    private SwipeRefreshLayout mLayout;
    private TopicListLoader mLoader;
    private FloatTopicPresenter mFloatTopic;
    private RecyclerView mRecyclerView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TopicListFragment.
     */
    public static TopicListFragment newInstance(Page page) {
        TopicListFragment fragment = new TopicListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    public TopicListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mPage = arguments.getParcelable(ARG_PAGE);
        }

        if (mPage == null) {
            throw new FatalException("node can't be null");
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_topic_list,
                container, false);
        mRecyclerView = ((RecyclerView) mLayout.findViewById(R.id.recycle_view));

        mLayout.setOnRefreshListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mLayout.getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        mAdapter = new TopicAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mLayout.setRefreshing(true);
        return mLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity activity = ((MainActivity) getActivity());

        boolean shouldSetTitle;
        if (mPage instanceof Node) {
            Node node = (Node) mPage;
            if (!node.hasInfo()) {
                node = NodeDao.get(node.getName());
                if (node == null) {
                    return;
                }
                mPage = node;
            }
            shouldSetTitle = true;
        } else if (mPage == Page.PAGE_FAV_TOPIC) {
            activity.setNavSelected(R.id.drawer_favorite);
            shouldSetTitle = true;
        } else {
            shouldSetTitle = false;
        }

        if (shouldSetTitle) {
            activity.setTitle(mPage.getTitle());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        final LoaderManager loaderManager = getLoaderManager();
        if (loaderManager.getLoader(0) != null) {
            // already loaded
            return;
        }
        loaderManager.initLoader(0, null, this);
    }

    @Override
    public void onStop() {
        super.onStop();

        AppCtx.getEventBus().unregister(this);
    }

    @Override
    public Loader<LoaderResult<List<Topic>>> onCreateLoader(int id, Bundle args) {
        String log = String.format("load list: %s", mPage.getTitle());
        Crashlytics.log(log);
        LogUtils.d(TAG, log);

        mLoader = new TopicListLoader(getActivity(), mPage);
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<List<Topic>>> loader, LoaderResult<List<Topic>> result) {
        mLayout.setRefreshing(false);
        if (result.hasException()) {
            ExceptionUtils.handleExceptionNoCatch(this, result.mException);
            return;
        }
        mAdapter.setDataSource(result.mResult);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<List<Topic>>> loader) {
        mAdapter.setDataSource(null);
    }

    @Override
    public void onRefresh() {
        final Loader<?> loader = getLoaderManager().getLoader(0);
        if (loader == null) {
            return;
        }
        loader.forceLoad();

        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_topic_list, menu);
        if (!UserState.getInstance().isLoggedIn()) {
            menu.findItem(R.id.action_new_topic).setVisible(false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mLayout.setRefreshing(true);
                onRefresh();
                return true;
            case R.id.action_new_topic:
                final Intent intent = new Intent(getActivity(), TopicEditActivity.class);
                if (mPage instanceof Node) {
                    intent.putExtra(TopicEditActivity.KEY_NODE, mPage);
                }
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTopicOpen(View view, Topic topic) {
        final Intent intent = new Intent(getContext(), TopicActivity.class);
        intent.putExtra(TopicActivity.KEY_TOPIC, topic);

        startActivity(intent);

        topic.setHasRead();
    }

    @Override
    public void onTopicStartPreview(View view, Topic topic) {
        TrackerUtils.onTopicForceTouch();

        final Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(20);

        getFloatTopic().fillData(topic);
        getFloatTopic().setVisibility(View.VISIBLE);
    }

    @Override
    public void onTopicStopPreview(View view, Topic topic) {
        getFloatTopic().setVisibility(View.INVISIBLE);
    }

    private FloatTopicPresenter getFloatTopic() {
        if (mFloatTopic == null) {
            mFloatTopic = new FloatTopicPresenter(getContext());
        }

        return mFloatTopic;
    }
}
