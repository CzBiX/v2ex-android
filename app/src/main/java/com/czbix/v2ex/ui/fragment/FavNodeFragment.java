package com.czbix.v2ex.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.czbix.v2ex.R;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.ui.MainActivity;
import com.czbix.v2ex.ui.adapter.NodeAdapter;
import com.czbix.v2ex.ui.loader.AsyncTaskLoader;
import com.czbix.v2ex.ui.loader.AsyncTaskLoader.LoaderResult;
import com.czbix.v2ex.util.ExceptionUtils;

import java.util.List;

public class FavNodeFragment extends Fragment implements LoaderCallbacks<LoaderResult<List<Node>>>,
        SwipeRefreshLayout.OnRefreshListener, NodeListFragment.OnNodeActionListener {
    private NodeAdapter mAdapter;
    private SwipeRefreshLayout mLayout;

    public static FavNodeFragment newInstance() {
        return new FavNodeFragment();
    }

    public FavNodeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_node_list, container, false);
        final RecyclerView recyclerView = (RecyclerView) mLayout.findViewById(R.id.recycle_view);

        mLayout.setOnRefreshListener(this);

        final LayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new NodeAdapter(this);
        recyclerView.setAdapter(mAdapter);

        mLayout.setRefreshing(true);
        return mLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<LoaderResult<List<Node>>> onCreateLoader(int id, Bundle args) {
        return new NodeLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<List<Node>>> loader, LoaderResult<List<Node>> result) {
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
    public void onLoaderReset(Loader<LoaderResult<List<Node>>> loader) {
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
    public void onNodeOpen(Node node) {
        final Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra(MainActivity.BUNDLE_NODE, node);
        startActivity(intent);
    }

    private static class NodeLoader extends AsyncTaskLoader<List<Node>> {
        public NodeLoader(Context context) {
            super(context);
        }

        @Override
        public List<Node> loadInBackgroundWithException() throws ConnectionException, RemoteException {
            return RequestHelper.INSTANCE.getFavNodes();
        }
    }
}
