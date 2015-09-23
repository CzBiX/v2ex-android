package com.czbix.v2ex.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.czbix.v2ex.R;
import com.czbix.v2ex.helper.CustomTabsHelper;
import com.czbix.v2ex.util.ExecutorUtils;
import com.google.common.base.Preconditions;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SearchActivity extends AppCompatActivity implements TextWatcher, TextView.OnEditorActionListener {
    private static final String TAG = SearchActivity.class.getSimpleName();
    private static final int DELAY_BEFORE_PREFETCH = 400;

    private CustomTabsHelper mCustomTabsHelper;
    private ScheduledFuture<?> mPrefetchTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mCustomTabsHelper = new CustomTabsHelper();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        Preconditions.checkNotNull(actionBar, "action bar shouldn't be null");

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        final EditText editText = (EditText) findViewById(R.id.search);
        editText.addTextChangedListener(this);
        editText.setOnEditorActionListener(this);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openSearch(String query) {
        final Uri uri = getSearchUri(query);

        final CustomTabsIntent.Builder builder = CustomTabsHelper.getBuilder(this,
                mCustomTabsHelper.getSession());
        final CustomTabsIntent intent = builder.build();
        intent.launchUrl(this, uri);
    }

    private Uri getSearchUri(String query) {
        final String queryToSearch = query + " site:https://www.v2ex.com";

        return new Uri.Builder().scheme("https").authority("www.google.com")
                .path("/search").appendQueryParameter("q", queryToSearch).build();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (TextUtils.isEmpty(editable)) {
            return;
        }

        final String newText = editable.toString();

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
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != EditorInfo.IME_ACTION_SEARCH) {
            return false;
        }

        final String query = v.getText().toString();
        if (TextUtils.isEmpty(query)) {
            return false;
        }

        if (mPrefetchTask != null) {
            mPrefetchTask.cancel(true);
        }
        openSearch(query);
        finish();

        return true;
    }
}
