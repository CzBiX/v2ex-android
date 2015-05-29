package com.czbix.v2ex.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.czbix.v2ex.dao.ConfigDao;

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
        button.setText("Logout");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfigDao.remove(ConfigDao.KEY_USERNAME);
            }
        });
        mLayout.addView(button);
    }
}
