package com.czbix.v2ex.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.czbix.v2ex.dao.NodeDao;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.util.UserUtils;

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

                final Member member = new Member.Builder().setUsername("CzBiX")
                        .setAvatar(UserUtils.getAvatar()).createMember();
                final Topic topic = new Topic.Builder()
                        .setId(130605)
                        .setTitle("Test")
                        .setNode(NodeDao.get("sandbox"))
                        .setMember(member)
                        .createTopic();
                intent.putExtra(TopicActivity.KEY_TOPIC, topic);

                startActivity(intent);
            }
        });
        mLayout.addView(button);
    }
}
