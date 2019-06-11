package com.czbix.v2ex.ui.widget;

import android.content.Context;
import androidx.appcompat.widget.SearchView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.czbix.v2ex.R;

/**
 * a listview with search feature
 */
public class SearchListView extends LinearLayout implements SearchView.OnQueryTextListener {
    private SearchView mSearchView;
    private ListView mListView;
    private ExArrayAdapter<?> mAdapter;
    private View mLoading;

    public SearchListView(Context context) {
        this(context, null);
    }

    public SearchListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setOrientation(VERTICAL);

        inflate(getContext(), R.layout.view_select_node, this);

        mSearchView = ((SearchView) findViewById(R.id.search));
        mLoading = findViewById(R.id.loading);
        mListView = ((ListView) findViewById(R.id.select_dialog_listview));

        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setIconifiedByDefault(false);
        mListView.setVisibility(GONE);
    }

    public void setAdapter(ExArrayAdapter<?> adapter) {
        if (mAdapter == null) {
            mLoading.setVisibility(GONE);
            mListView.setVisibility(VISIBLE);
        }
        mAdapter = adapter;
        mListView.setAdapter(adapter);

        mAdapter.getFilter().filter(mSearchView.getQuery());
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mListView.setOnItemClickListener(listener);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (mAdapter == null) {
            return true;
        }
        mAdapter.getFilter().filter(newText);
        return true;
    }
}
