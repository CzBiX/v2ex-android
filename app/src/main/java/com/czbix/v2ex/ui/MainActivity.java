package com.czbix.v2ex.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.eventbus.BusEvent;
import com.czbix.v2ex.model.Tab;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.fragment.TopicFragment;
import com.czbix.v2ex.ui.fragment.TopicListFragment;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;


public class MainActivity extends AppCompatActivity implements TopicListFragment.TopicListActionListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean mRegisteredEventBus;
    private TextView mUsername;
    private AppBarLayout mAppBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsername = (TextView) findViewById(R.id.username_tv);
        mAppBar = ((AppBarLayout) findViewById(R.id.appbar));

        initToolbar();
        updateUsername();
        if (savedInstanceState == null) {
            addFragmentToView();
        }
    }

    private void updateUsername() {
        if (Strings.isNullOrEmpty(AppCtx.getInstance().getUsername())) {
            mUsername.setVisibility(View.INVISIBLE);
        } else {
            mUsername.setText(AppCtx.getInstance().getUsername());
        }
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }

        setSupportActionBar(toolbar);
    }

    private void addFragmentToView() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final TopicListFragment fragment = TopicListFragment.newInstance(Tab.TAB_ALL);
        fragmentManager.beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        enableLoginMenu(menu);

        return true;
    }

    private void enableLoginMenu(Menu menu) {
        if (!Strings.isNullOrEmpty(AppCtx.getInstance().getUsername())) {
            return;
        }

        // not sign in yet
        AppCtx.getEventBus().register(this);
        mRegisteredEventBus = true;
        final MenuItem loginMenu = menu.add(R.string.action_sign_in);
        loginMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return true;
            }
        });
    }

    @Subscribe
    public void onLoginEvent(BusEvent.LoginEvent e) {
        invalidateOptionsMenu();
        updateUsername();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRegisteredEventBus) {
            AppCtx.getEventBus().unregister(this);
            mRegisteredEventBus = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTopicOpen(View view, Topic topic) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, TopicFragment.newInstance(topic))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }
}
