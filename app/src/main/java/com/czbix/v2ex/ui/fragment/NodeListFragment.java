package com.czbix.v2ex.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.czbix.v2ex.R;
import com.czbix.v2ex.dao.NodeDao;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.ui.MainActivity;
import com.czbix.v2ex.ui.adapter.NodeAdapter;
import com.czbix.v2ex.ui.loader.AsyncTaskLoader;
import com.czbix.v2ex.ui.loader.AsyncTaskLoader.LoaderResult;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnNodeActionListener} interface
 * to handle interaction events.
 * Use the {@link NodeListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NodeListFragment extends Fragment implements LoaderCallbacks<LoaderResult<List<Node>>>, SearchView.OnQueryTextListener {
    private OnNodeActionListener mListener;
    private NodeAdapter mAdapter;
    private CharSequence mQueryText;

    public static NodeListFragment newInstance() {
        return new NodeListFragment();
    }

    public NodeListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_nodes, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(this);
        if (mQueryText != null) {
            searchView.setIconified(false);
            searchView.setQuery(mQueryText, false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View layout = inflater.inflate(R.layout.recycle_view, container, false);
        final RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.recycle_view);

        final LayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new NodeAdapter(mListener);
        recyclerView.setAdapter(mAdapter);

        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnNodeActionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnNodeActionListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity activity = (MainActivity) getActivity();
        activity.setNavSelected(R.id.drawer_nodes);
        activity.setTitle(R.string.title_fragment_nodes);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<LoaderResult<List<Node>>> onCreateLoader(int id, Bundle args) {
        return new NodeLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<List<Node>>> loader, LoaderResult<List<Node>> result) {
        Preconditions.checkState(!result.hasException());
        mAdapter.setDataSource(result.mResult);
        mAdapter.filterText(mQueryText);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<List<Node>>> loader) {
        mAdapter.setDataSource(null);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAdapter.filterText(newText);
        mQueryText = newText;
        return true;
    }

    public interface OnNodeActionListener {
        void onNodeOpen(Node node);
    }

    private static class NodeLoader extends AsyncTaskLoader<List<Node>> {
        public NodeLoader(Context context) {
            super(context);
        }

        @Override
        public List<Node> loadInBackgroundWithException() {
            final List<Node> list = NodeDao.getAll();
            Collections.sort(list);

            return list;
        }
    }
}
