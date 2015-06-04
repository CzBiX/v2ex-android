package com.czbix.v2ex.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.fragment.TopicFragment;
import com.google.common.base.Strings;

public class TopicActivity extends AppCompatActivity {
    public static final String KEY_TOPIC = "topic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_fragment);

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
        if (intent.hasExtra(KEY_TOPIC)) {
            return intent.getParcelableExtra(KEY_TOPIC);
        }
        if(intent.getAction().equals(Intent.ACTION_VIEW)) {
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
