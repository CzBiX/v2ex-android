package com.czbix.v2ex.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.eventbus.LoginEvent;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.model.Tab;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.fragment.NodeListFragment;
import com.czbix.v2ex.ui.fragment.TopicListFragment;
import com.czbix.v2ex.util.UserUtils;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;


public class MainActivity extends AppCompatActivity implements TopicListFragment.TopicListActionListener,
        NavigationView.OnNavigationItemSelectedListener, NodeListFragment.OnNodeActionListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PREF_DRAWER_SHOWED = "drawer_showed";

    private TextView mUsername;
    private AppBarLayout mAppBar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNav;
    private ImageView mAvatar;
    private SharedPreferences mPreferences;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = getPreferences(MODE_PRIVATE);
        mAvatar = ((ImageView) findViewById(R.id.avatar_img));
        mUsername = (TextView) findViewById(R.id.username_tv);
        mAppBar = ((AppBarLayout) findViewById(R.id.appbar));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.layout);
        mNav = ((NavigationView) findViewById(R.id.nav));

        AppCtx.getEventBus().register(this);

        initToolbar();
        initNavDrawer();

        if (savedInstanceState == null) {
            addFragmentToView(TopicListFragment.newInstance(Tab.TAB_ALL));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateUsername();
    }

    private void initNavDrawer() {
        mNav.setNavigationItemSelectedListener(this);
        if (!mPreferences.getBoolean(PREF_DRAWER_SHOWED, false)) {
            mDrawerLayout.openDrawer(mNav);
            mPreferences.edit().putBoolean(PREF_DRAWER_SHOWED, true).apply();
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.desc_open_drawer, R.string.desc_close_drawer);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.isChecked()) return false;

        switch (item.getItemId()) {
            case R.id.drawer_explore:
                switchFragment(TopicListFragment.newInstance(Tab.TAB_ALL));
                return true;
            case R.id.drawer_nodes:
                switchFragment(NodeListFragment.newInstance());
                return true;
        }

        return false;
    }

    public void setNavSelected(@IdRes int menuId) {
        final Menu menu = mNav.getMenu();
        menu.findItem(menuId).setChecked(true);
    }

    private void switchFragment(Fragment fragment) {
        mDrawerLayout.closeDrawer(mNav);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    private void updateUsername() {
        if (Strings.isNullOrEmpty(AppCtx.getInstance().getUsername())) {
            mAvatar.setVisibility(View.INVISIBLE);
            mUsername.setText(R.string.user_anonymous);
            return;
        }

        mAvatar.setVisibility(View.VISIBLE);
        final Avatar avatar = UserUtils.getAvatar();
        Glide.with(this).load(avatar.getUrlByDp(getResources().getDimension(R.dimen.nav_avatar_size)))
                .crossFade().into(mAvatar);
        mUsername.setText(AppCtx.getInstance().getUsername());
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            return;
        }

        setSupportActionBar(mToolbar);
    }

    private void addFragmentToView(Fragment fragment) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
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
        if (AppCtx.getInstance().isLoggedIn()) {
            return;
        }

        // not sign in yet
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
    public void onLoginEvent(LoginEvent e) {
        invalidateOptionsMenu();

        updateUsername();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AppCtx.getEventBus().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNav)) {
            mDrawerLayout.closeDrawer(mNav);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onTopicOpen(View view, Topic topic) {
        final Intent intent = new Intent(this, TopicActivity.class);
        intent.putExtra(TopicActivity.KEY_TOPIC, topic);

        startActivity(intent);
    }

    @Override
    public void onNodeClick(Node node) {
        final TopicListFragment topicListFragment = TopicListFragment.newInstance(node);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, topicListFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }
}
