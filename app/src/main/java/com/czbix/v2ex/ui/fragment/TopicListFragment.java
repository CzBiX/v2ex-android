package com.czbix.v2ex.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.dao.ConfigDao;
import com.czbix.v2ex.eventbus.BusEvent;
import com.czbix.v2ex.model.Page;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.MainActivity;
import com.czbix.v2ex.ui.adapter.TopicAdapter;
import com.czbix.v2ex.ui.loader.TopicListLoader;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TopicListActionListener} interface
 * to handle interaction events.
 * Use the {@link TopicListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopicListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Topic>>,TopicAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = TopicListFragment.class.getSimpleName();
    private static final String ARG_PAGE = "page";

    private Page mPage;

    private TopicListActionListener mListener;
    private RecyclerView mRecyclerView;
    private TopicAdapter mAdapter;
    private SwipeRefreshLayout mLayout;
    private ActionBar mActionBar;
    private LinearLayoutManager mLayoutManager;

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
        if (getArguments() != null) {
            mPage = getArguments().getParcelable(ARG_PAGE);
        }

        if (mPage == null) {
            throw new FatalException("node can't be null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_topic_list,
                container, false);
        mLayout.setOnRefreshListener(this);

        mRecyclerView = ((RecyclerView) mLayout.findViewById(R.id.recycle_view));
        mLayoutManager = new LinearLayoutManager(mLayout.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new TopicAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mLayout.setRefreshing(true);
        return mLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity activity = (MainActivity) getActivity();
        activity.setTitle(mPage.getTitle());

        mActionBar = activity.getSupportActionBar();
        Preconditions.checkNotNull(mActionBar);
        mActionBar.setDisplayHomeAsUpEnabled(false);

        AppCtx.getEventBus().register(this);

        if (ConfigDao.get(ConfigDao.KEY_NODE_ETAG, null) != null) {
            onNodesLoadFinish(null);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (TopicListActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TopicListActionListener");
        }
    }

    @Subscribe
    public void onNodesLoadFinish(BusEvent.GetNodesFinishEvent e) {
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        AppCtx.getEventBus().unregister(this);
    }

    @Override
    public Loader<List<Topic>> onCreateLoader(int id, Bundle args) {
        return new TopicListLoader(getActivity(), mPage);
    }

    @Override
    public void onLoadFinished(Loader<List<Topic>> loader, List<Topic> data) {
        mAdapter.setDataSource(data);
        mLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<List<Topic>> loader) {
        mAdapter.setDataSource(null);
    }

    @Override
    public void onItemClick(int position, Topic topic) {
        mListener.onTopicOpen(topic);
    }

    @Override
    public void onRefresh() {
        final Loader<?> loader = getLoaderManager().getLoader(0);
        if (loader == null) {
            return;
        }
        loader.forceLoad();
    }

    public interface TopicListActionListener {
        void onTopicOpen(Topic topic);
    }

}
