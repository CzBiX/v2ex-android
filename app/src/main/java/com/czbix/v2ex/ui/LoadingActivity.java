package com.czbix.v2ex.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.czbix.v2ex.R;

public class LoadingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, R.string.toast_please_wait, Toast.LENGTH_SHORT).show();
    }
}
