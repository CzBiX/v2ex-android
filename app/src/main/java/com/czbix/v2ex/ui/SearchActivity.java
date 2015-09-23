package com.czbix.v2ex.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.czbix.v2ex.R;
import com.czbix.v2ex.helper.CustomTabsHelper;
import com.czbix.v2ex.util.ExecutorUtils;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = SearchActivity.class.getSimpleName();
    private static final int DELAY_BEFORE_PREFETCH = 400;

    private CustomTabsHelper mCustomTabsHelper;
    private ScheduledFuture<?> mPrefetchTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final ActionBar actionBar = getSupportActionBar();
        Preconditions.checkNotNull(actionBar, "action bar shouldn't be null");

        actionBar.setDisplayHomeAsUpEnabled(true);

        mCustomTabsHelper = new CustomTabsHelper();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mCustomTabsHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mCustomTabsHelper.unbindCustomTabsService(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nodes, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.requestFocus();

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
        if (mPrefetchTask != null) {
            mPrefetchTask.cancel(true);
        }
        openSearch(query);
        finish();

        return true;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        if (Strings.isNullOrEmpty(newText)) {
            return false;
        }

        if (mPrefetchTask != null) {
            mPrefetchTask.cancel(true);
        }

        if (!newText.endsWith(" ")) {
            mPrefetchTask = ExecutorUtils.schedule(new Runnable() {
                @Override
                public void run() {
                    mCustomTabsHelper.mayLaunchUrl(getSearchUri(newText), null, null);
                }
            }, DELAY_BEFORE_PREFETCH, TimeUnit.MILLISECONDS);
        }

        return false;
    }

    private void openSearch(String query) {
        final Uri uri = getSearchUri(query);

        final CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(mCustomTabsHelper.getSession());
        final int colorPrimary = ViewUtils.getAttrColor(getTheme(),
                android.support.v7.appcompat.R.attr.colorPrimary);
        builder.setToolbarColor(colorPrimary)
                .setShowTitle(true);

        final CustomTabsIntent intent = builder.build();
        intent.launchUrl(this, uri);
    }

    private Uri getSearchUri(String query) {
        final String queryToSearch = query + " site:https://www.v2ex.com";

        return new Uri.Builder().scheme("https").authority("www.google.com")
                .path("/search").appendQueryParameter("q", queryToSearch).build();
    }
}
