package com.czbix.v2ex.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.fragment.TopicFragment;
import com.czbix.v2ex.util.CrashlyticsUtils;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Strings;

public class TopicActivity extends BaseActivity {
    private static final String TAG = TopicActivity.class.getSimpleName();

    public static final String KEY_TOPIC = "topic";
    public static final String KEY_TOPIC_ID = "topic_id";
    private AppBarLayout mAppBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        ViewUtils.initToolbar(this);
        mAppBarLayout = findViewById(R.id.appbar);

        if (savedInstanceState == null) {
            Topic topic = getTopicFromIntent();
            if (topic == null) {
                finish();
                return;
            }

            addFragmentToView(topic);
        }
    }

    public AppBarLayout getAppBarLayout() {
        return mAppBarLayout;
    }

    private Topic getTopicFromIntent() {
        final Intent intent = getIntent();
        if (intent == null) {
            return null;
        }

        if (intent.hasExtra(KEY_TOPIC)) {
            return intent.getParcelableExtra(KEY_TOPIC);
        }
        if (intent.hasExtra(KEY_TOPIC_ID)) {
            final int id = intent.getIntExtra(KEY_TOPIC_ID, 0);
            return new Topic.Builder().setId(id).createTopic();
        }

        if(intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW)) {
            final String url = intent.getDataString();
            if (!Strings.isNullOrEmpty(url)) {
                final int id;
                try {
                    id = Topic.getIdFromUrl(url);
                } catch (IllegalArgumentException e) {
                    Crashlytics.log(Log.INFO, TAG, "unsupported url: " + url);
                    Toast.makeText(this, R.string.toast_unsupported_url, Toast.LENGTH_LONG).show();

                    return null;
                }
                return new Topic.Builder().setId(id).createTopic();

            }
        }

        return null;
    }

    private void addFragmentToView(Topic topic) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, TopicFragment.newInstance(topic))
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
