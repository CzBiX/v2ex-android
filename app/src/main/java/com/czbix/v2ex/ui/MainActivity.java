package com.czbix.v2ex.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.dao.NodeDao;
import com.czbix.v2ex.eventbus.BusEvent.DailyAwardEvent;
import com.czbix.v2ex.eventbus.BusEvent.NewUnreadEvent;
import com.czbix.v2ex.eventbus.LoginEvent;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.model.Tab;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.loader.GooglePhotoUrlLoader;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.res.GoogleImg;
import com.czbix.v2ex.ui.fragment.CategoryTabFragment;
import com.czbix.v2ex.ui.fragment.NodeListFragment;
import com.czbix.v2ex.ui.fragment.TopicListFragment;
import com.czbix.v2ex.util.ExecutorUtils;
import com.czbix.v2ex.util.UserUtils;
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
    private View mNavBg;
    private Node mLastNode;
    private MenuItem mNotificationsItem;
    private View mAwardButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = getPreferences(MODE_PRIVATE);
        mAvatar = ((ImageView) findViewById(R.id.avatar_img));
        mUsername = (TextView) findViewById(R.id.username_tv);
        mAwardButton = findViewById(R.id.award);
        mAppBar = ((AppBarLayout) findViewById(R.id.appbar));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNav = ((NavigationView) findViewById(R.id.nav));
        mNavBg = mNav.findViewById(R.id.nav_layout);

        initToolbar();
        initNavDrawer();

        addFragmentToView(getFragmentToShow());
    }

    private Fragment getFragmentToShow() {
        final Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            final String url = intent.getDataString();
            final String name = Node.getNameFromUrl(url);
            final Node node = NodeDao.get(name);
            if (node != null) {
                return TopicListFragment.newInstance(node);
            }
        }

        return CategoryTabFragment.newInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        AppCtx.getEventBus().register(this);

        updateUsername();
        updateNavBackground();
        updateNotifications();
        setAwardVisibility(UserState.getInstance().hasAward());
    }

    private void updateNotifications() {
        if (UserState.getInstance().isGuest()) {
            mNotificationsItem.setEnabled(false);
        } else {
            mNotificationsItem.setEnabled(true);
            mNotificationsItem.setIcon(UserState.getInstance().hasUnread()
                    ? R.drawable.ic_notifications_black_24dp
                    : R.drawable.ic_notifications_none_black_24dp);
        }
    }

    private void updateNavBackground() {
        String url = GoogleImg.ALL_LOCATION[GoogleImg.getLocationIndex()][GoogleImg.getTimeIndex()];
        Glide.with(this).using(GooglePhotoUrlLoader.getInstance()).load(url)
                .crossFade().centerCrop().into(new ViewTarget<View, GlideDrawable>(mNavBg) {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                mNavBg.setBackground(resource);
            }
        });
    }

    @Subscribe
    public void onDailyMissionEvent(DailyAwardEvent e) {
        if (!e.mHasAward) {
            Toast.makeText(this, R.string.toast_daily_award_received, Toast.LENGTH_LONG).show();
        }
        setAwardVisibility(e.mHasAward);
    }

    private void setAwardVisibility(boolean visibility) {
        mAwardButton.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
    }

    private void initNavDrawer() {
        mNav.setNavigationItemSelectedListener(this);
        if (!mPreferences.getBoolean(PREF_DRAWER_SHOWED, false)) {
            mDrawerLayout.openDrawer(mNav);
            mPreferences.edit().putBoolean(PREF_DRAWER_SHOWED, true).apply();
        }
        final Menu menu = mNav.getMenu();
        mNotificationsItem = menu.findItem(R.id.drawer_notifications);
        // not implemented yet
        mNotificationsItem.setVisible(false);
//        updateNotifications();

        mAwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExecutorUtils.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            RequestHelper.dailyMission();
                        } catch (ConnectionException | RemoteException e) {
                            e.printStackTrace();
                        }

                        AppCtx.getEventBus().post(new DailyAwardEvent(false));
                    }
                });
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.desc_open_drawer, R.string.desc_close_drawer);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mNavBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserState.getInstance().isGuest()) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } else {
                    // TODO: jump to user info page
                }
            }
        });
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
            case R.id.drawer_settings:
                startActivity(new Intent(this, SettingsActivity.class));
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
        if (UserState.getInstance().isGuest()) {
            mAvatar.setVisibility(View.INVISIBLE);
            mUsername.setText(R.string.action_sign_in);
            return;
        }

        mAvatar.setVisibility(View.VISIBLE);
        final Avatar avatar = UserUtils.getAvatar();
        Glide.with(this).load(avatar.getUrlByDp(getResources().getDimension(R.dimen.nav_avatar_size)))
                .crossFade().into(mAvatar);
        mUsername.setText(UserState.getInstance().getUsername());
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
        enableLoginMenu(menu);

        return true;
    }

    private void enableLoginMenu(Menu menu) {
        if (!UserState.getInstance().isGuest()) {
            return;
        }

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
        updateNotifications();
    }

    @Subscribe
    public void onNewUnreadEvent(NewUnreadEvent e) {
        updateNotifications();
    }

    @Override
    protected void onStop() {
        super.onStop();

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

        mLastNode = node;
    }
}
