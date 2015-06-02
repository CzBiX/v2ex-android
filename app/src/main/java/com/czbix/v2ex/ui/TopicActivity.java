package com.czbix.v2ex.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.fragment.TopicFragment;
import com.google.common.base.Preconditions;

public class TopicActivity extends AppCompatActivity {
    public static final String KEY_TOPIC = "topic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_fragment);

        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            Preconditions.checkState(intent.hasExtra(KEY_TOPIC));
            addFragmentToView(intent.<Topic>getParcelableExtra(KEY_TOPIC));
        }
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
