package com.czbix.v2ex.ui;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.czbix.v2ex.R;
import com.czbix.v2ex.util.LogUtils;
import com.google.common.base.Preconditions;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = SearchActivity.class.getSimpleName();
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final ActionBar actionBar = getSupportActionBar();
        Preconditions.checkNotNull(actionBar, "action bar shouldn't be null");

        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nodes, menu);

        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.requestFocus();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        openSearch(query);
        finish();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void openSearch(String query) {
        final String queryToSearch = "site:https://www.v2ex.com " + query;

        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, queryToSearch);
        try {
            startActivity(intent);
            return;
        } catch (ActivityNotFoundException e) {
            LogUtils.v(TAG, "can't found activity to handle web search", e);
        }

        final Uri uri = new Uri.Builder().scheme("https").authority("www.google.com")
                .path("/search").appendQueryParameter("q", queryToSearch).build();
        intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
