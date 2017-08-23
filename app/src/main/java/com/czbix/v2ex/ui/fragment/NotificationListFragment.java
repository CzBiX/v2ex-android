package com.czbix.v2ex.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
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

import com.czbix.v2ex.R;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.model.Notification;
import com.czbix.v2ex.ui.MainActivity;
import com.czbix.v2ex.ui.TopicActivity;
import com.czbix.v2ex.ui.adapter.NotificationAdapter;
import com.czbix.v2ex.ui.loader.AsyncTaskLoader.LoaderResult;
import com.czbix.v2ex.ui.loader.NotificationLoader;
import com.czbix.v2ex.ui.widget.DividerItemDecoration;
import com.czbix.v2ex.util.ExceptionUtils;

import java.util.List;

public class NotificationListFragment extends Fragment implements LoaderManager.LoaderCallbacks<LoaderResult<List<Notification>>>, SwipeRefreshLayout.OnRefreshListener, NotificationAdapter.OnNotificationActionListener {
    private NotificationAdapter mAdapter;
    private SwipeRefreshLayout mLayout;

    public NotificationListFragment() {
    }

    public static NotificationListFragment newInstance() {
        return new NotificationListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_notification_list, container, false);
        final RecyclerView recyclerView = (RecyclerView) mLayout.findViewById(R.id.recycle_view);

        mLayout.setColorSchemeResources(R.color.material_blue_grey_500, R.color.material_blue_grey_700, R.color.material_blue_grey_900);
        mLayout.setOnRefreshListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        mAdapter = new NotificationAdapter(this);
        recyclerView.setAdapter(mAdapter);

        mLayout.setRefreshing(true);
        return mLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity activity = (MainActivity) getActivity();
        activity.setNavSelected(R.id.drawer_notifications);
        activity.setTitle(R.string.title_fragment_notifications);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();

        UserState.INSTANCE.clearUnread();
    }

    @Override
    public Loader<LoaderResult<List<Notification>>> onCreateLoader(int id, Bundle args) {
        return new NotificationLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<List<Notification>>> loader, LoaderResult<List<Notification>> result) {
        mLayout.setRefreshing(false);
        if (result.hasException()) {
            if (ExceptionUtils.handleExceptionNoCatch(this, result.mException)) {
                getActivity().finish();
            }
            return;
        }

        mAdapter.setDataSource(result.mResult);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<List<Notification>>> loader) {
        mAdapter.setDataSource(null);
    }

    @Override
    public void onRefresh() {
        final Loader<?> loader = getLoaderManager().getLoader(0);
        if (loader == null) {
            return;
        }
        loader.forceLoad();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_topic_list, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mLayout.setRefreshing(true);
                onRefresh();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNotificationOpen(Notification notification) {
        final Intent intent = new Intent(getActivity(), TopicActivity.class);
        intent.putExtra(TopicActivity.KEY_TOPIC, notification.mTopic);
        startActivity(intent);
    }
}
