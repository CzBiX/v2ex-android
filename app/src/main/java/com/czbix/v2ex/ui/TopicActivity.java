package com.czbix.v2ex.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.fragment.TopicFragment;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Strings;

public class TopicActivity extends BaseActivity {
    public static final String KEY_TOPIC = "topic";
    public static final String KEY_TOPIC_ID = "topic_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        ViewUtils.initToolbar(this);

        if (savedInstanceState == null) {
            Topic topic = getTopicFromIntent();
            if (topic == null) {
                finish();
                return;
            }

            addFragmentToView(topic);
        }
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
                final int id = Topic.getIdFromUrl(url);

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
