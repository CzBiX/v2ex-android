package com.czbix.v2ex.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.db.ViewHistory;
import com.czbix.v2ex.ui.TopicActivity;
import com.czbix.v2ex.ui.adapter.TopicAdapter;
import com.czbix.v2ex.ui.loader.AsyncTaskLoader.LoaderResult;
import com.czbix.v2ex.ui.loader.ViewHistoryLoader;
import com.czbix.v2ex.ui.widget.DividerItemDecoration;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Preconditions;

import java.util.List;

public class HistoryFragment extends Fragment implements LoaderCallbacks<LoaderResult<List<ViewHistory>>>,TopicAdapter.OnTopicActionListener {
    private ViewHistoryAdapter mAdapter;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.recycle_view,
                container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        mAdapter = new ViewHistoryAdapter(this);
        recyclerView.setAdapter(mAdapter);

        return recyclerView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.title_fragment_history);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onTopicOpen(View view, Topic topic) {
        final Intent intent = new Intent(getActivity(), TopicActivity.class);
        intent.putExtra(TopicActivity.KEY_TOPIC, topic);
        startActivity(intent);

        return false;
    }

    @Override
    public Loader<LoaderResult<List<ViewHistory>>> onCreateLoader(int id, Bundle args) {
        return new ViewHistoryLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<List<ViewHistory>>> loader, LoaderResult<List<ViewHistory>> data) {
        Preconditions.checkState(!data.hasException());
        mAdapter.setDataSource(data.mResult);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<List<ViewHistory>>> loader) {
        mAdapter.setDataSource(null);
    }

    private static class ViewHistoryAdapter extends RecyclerView.Adapter<ViewHistoryAdapter.ViewHolder> {
        private final TopicAdapter.OnTopicActionListener mListener;
        private List<ViewHistory> mData;

        public ViewHistoryAdapter(TopicAdapter.OnTopicActionListener listener) {
            mListener = listener;
        }

        public void setDataSource(List<ViewHistory> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_history, parent, false);
            return new ViewHolder(view, mListener);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.fillData(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TopicAdapter.OnTopicActionListener mListener;
            private final TextView mTitle;
            private final TextView mTime;
            private ViewHistory mHistory;

            public ViewHolder(View view, TopicAdapter.OnTopicActionListener listener) {
                super(view);

                mListener = listener;
                view.setOnClickListener(this);

                mTitle = ((TextView) view.findViewById(R.id.title));
                mTime = (TextView) view.findViewById(R.id.time);
            }

            public void fillData(ViewHistory history) {
                if (history.equals(mHistory)) {
                    return;
                }
                mHistory = history;

                ViewUtils.setHtmlIntoTextViewWithPixel(mTitle, history.mTopic.getTitle(),
                        mTitle.getResources().getDimensionPixelSize(
                                R.dimen.abc_text_size_body_1_material));

                mTime.setText(DateUtils.getRelativeTimeSpanString(AppCtx.getInstance(),
                        history.mTime));
            }

            @Override
            public void onClick(View v) {
                mListener.onTopicOpen(v, mHistory.mTopic);
            }
        }
    }
}
