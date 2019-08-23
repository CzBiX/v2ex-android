package com.czbix.v2ex.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.airbnb.epoxy.EpoxyRecyclerView;
import com.czbix.v2ex.R;
import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.dao.NodeDao;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.ui.MainActivity;
import com.czbix.v2ex.ui.adapter.NodeController;
import com.czbix.v2ex.ui.loader.AsyncTaskLoader;
import com.czbix.v2ex.ui.loader.AsyncTaskLoader.LoaderResult;

import java.util.Collections;
import java.util.List;

public class NodeListFragment extends Fragment implements LoaderCallbacks<LoaderResult<List<Node>>>, SearchView.OnQueryTextListener {
    private OnNodeActionListener mListener;
    private NodeController controller;
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
        final View layout = inflater.inflate(R.layout.fragment_node_list, container, false);
        final EpoxyRecyclerView recyclerView = layout.findViewById(R.id.recycle_view);

        final LayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        controller = new NodeController(mListener);
        recyclerView.setController(controller);

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
        if (result.hasException()) {
            throw new FatalException(result.mException);
        }

        controller.setData(result.mResult);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<List<Node>>> loader) {
        controller.setData(null);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        controller.filterText(newText);
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
