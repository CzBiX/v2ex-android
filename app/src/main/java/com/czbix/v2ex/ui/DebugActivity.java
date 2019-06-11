package com.czbix.v2ex.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.czbix.v2ex.model.Topic;

public class DebugActivity extends AppCompatActivity {

    private LinearLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayout = new LinearLayout(this);
        setContentView(mLayout);

        initDebugItem();
    }

    private void initDebugItem() {
        Button button = new Button(this);
        button.setText("Sandbox Topic");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(DebugActivity.this, TopicActivity.class);

                final Topic topic = new Topic.Builder().setId(130605).createTopic();
                intent.putExtra(TopicActivity.KEY_TOPIC, topic);

                startActivity(intent);
            }
        });
        mLayout.addView(button);
    }
}
