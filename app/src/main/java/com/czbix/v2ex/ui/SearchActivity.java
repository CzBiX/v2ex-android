package com.czbix.v2ex.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.czbix.v2ex.R;
import com.czbix.v2ex.presenter.TopicSearchPresenter;
import com.google.common.base.Preconditions;

public class SearchActivity extends AppCompatActivity implements TextWatcher, TextView.OnEditorActionListener {
    private static final String TAG = SearchActivity.class.getSimpleName();
    private TopicSearchPresenter mTopicSearchPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mTopicSearchPresenter = new TopicSearchPresenter(this);

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

        mTopicSearchPresenter.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mTopicSearchPresenter.end();
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
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        final String newText = editable.toString();
        mTopicSearchPresenter.changeQuery(newText);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != EditorInfo.IME_ACTION_SEARCH) {
            return false;
        }

        final String query = v.getText().toString();
        if (!mTopicSearchPresenter.submitQuery(query)) {
            return false;
        }

        finish();
        return true;
    }
}
