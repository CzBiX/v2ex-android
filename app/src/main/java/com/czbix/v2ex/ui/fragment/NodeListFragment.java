package com.czbix.v2ex.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.czbix.v2ex.R;
import com.czbix.v2ex.dao.NodeDao;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.ui.MainActivity;
import com.czbix.v2ex.ui.loader.AsyncTaskLoader;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnNodeActionListener} interface
 * to handle interaction events.
 * Use the {@link NodeListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NodeListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Node>>, SearchView.OnQueryTextListener {
    private static final String KEY_QUERY = "query";

    private OnNodeActionListener mListener;
    private NodeAdapter mAdapter;
    private SearchView mSearchView;
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

        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setOnQueryTextListener(this);
        if (mQueryText != null) {
            mSearchView.setIconified(false);
            mSearchView.setQuery(mQueryText, false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mQueryText = savedInstanceState.getCharSequence(KEY_QUERY);
        } else {
            mQueryText = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View layout = inflater.inflate(R.layout.fragment_node_list, container, false);
        final RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.recycle_view);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(layout.getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new NodeAdapter(mListener);
        recyclerView.setAdapter(mAdapter);

        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnNodeActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnNodeActionListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity activity = (MainActivity) getActivity();
        activity.setNavSelected(R.id.drawer_nodes);
        activity.setTitle(R.string.title_fragment_nodes);

        final ActionBar actionBar = activity.getSupportActionBar();
        Preconditions.checkNotNull(actionBar);
        actionBar.setDisplayHomeAsUpEnabled(false);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final CharSequence query = mSearchView.getQuery();
        if (TextUtils.isEmpty(query)) {
            return;
        }

        outState.putCharSequence(KEY_QUERY, query);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<List<Node>> onCreateLoader(int id, Bundle args) {
        return new NodeLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Node>> loader, List<Node> data) {
        mAdapter.setDataSource(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Node>> loader) {
        mAdapter.setDataSource(null);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAdapter.filterText(newText);
        return true;
    }

    public interface OnNodeActionListener {
        void onNodeClick(Node node);
    }

    private static class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.ViewHolder> {
        private final OnNodeActionListener mListener;
        private List<Node> mData;
        private List<Node> mAllData;

        public NodeAdapter(OnNodeActionListener listener) {
            mListener = listener;

            setHasStableIds(true);
        }

        public void setDataSource(List<Node> data) {
            mData = data;
            mAllData = mData;
            notifyDataSetChanged();
        }

        public void filterText(String query) {
            if (Strings.isNullOrEmpty(query)) {
                mData = mAllData;
                notifyDataSetChanged();
                return;
            }

            List<Node> result = Lists.newArrayList();
            for (Node node : mAllData) {
                if (node.getName().contains(query) ||
                        node.getTitle().contains(query) ||
                        (node.getTitleAlternative() != null && node.getTitleAlternative().contains(query)))
                    result.add(node);
            }

            mData = result;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_node,
                    parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Node node = mData.get(position);
            holder.fillData(node);
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public long getItemId(int position) {
            return mData.get(position).getId();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView mTitle;
            private final TextView mAlertTitle;
            private final ImageView mAvatar;

            public ViewHolder(View view) {
                super(view);

                mTitle = ((TextView) view.findViewById(R.id.title));
                mAlertTitle = (TextView) view.findViewById(R.id.alertTitle);
                mAvatar = (ImageView) view.findViewById(R.id.avatar_img);

                view.setOnClickListener(this);
            }

            public void fillData(Node node) {
                mTitle.setText(node.getTitle());
                final String alternative = node.getTitleAlternative();
                if (Strings.isNullOrEmpty(alternative)) {
                    mAlertTitle.setVisibility(View.INVISIBLE);
                } else {
                    mAlertTitle.setVisibility(View.VISIBLE);
                    mAlertTitle.setText(alternative);
                }

                setAvatarImg(node);
            }

            @Override
            public void onClick(View v) {
                final int position = getAdapterPosition();
                mListener.onNodeClick(mData.get(position));
            }

            public void setAvatarImg(Node avatarImg) {
                final Avatar avatar = avatarImg.getAvatar();
                if (avatar == null) {
                    return;
                }

                final float dimen = mAvatar.getResources().getDimension(R.dimen.node_avatar_size);
                Glide.with(mAvatar.getContext()).load(avatar.getUrlByDp(dimen)).crossFade()
                        .into(mAvatar);
            }
        }
    }

    private static class NodeLoader extends AsyncTaskLoader<List<Node>> {
        public NodeLoader(Context context) {
            super(context);
        }

        @Override
        public List<Node> loadInBackground() {
            final List<Node> list = NodeDao.getAll();
            Collections.sort(list, new Comparator<Node>() {
                @Override
                public int compare(Node lhs, Node rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            });

            return list;
        }
    }
}
