package com.czbix.v2ex.ui;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.R;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.dao.TopicDao;
import com.czbix.v2ex.eventbus.BaseEvent.DailyAwardEvent;
import com.czbix.v2ex.eventbus.BaseEvent.NewUnreadEvent;
import com.czbix.v2ex.eventbus.LoginEvent;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.model.Page;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.loader.GooglePhotoUrlLoader;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.presenter.TopicSearchPresenter;
import com.czbix.v2ex.res.GoogleImg;
import com.czbix.v2ex.ui.adapter.TopicAdapter.OnTopicActionListener;
import com.czbix.v2ex.ui.fragment.CategoryTabFragment;
import com.czbix.v2ex.ui.fragment.NodeListFragment;
import com.czbix.v2ex.ui.fragment.NotificationListFragment;
import com.czbix.v2ex.ui.fragment.TopicListFragment;
import com.czbix.v2ex.ui.widget.SearchBoxLayout;
import com.czbix.v2ex.util.ExecutorUtils;
import com.czbix.v2ex.util.LogUtils;
import com.czbix.v2ex.util.MiscUtils;
import com.czbix.v2ex.util.TrackerUtils;
import com.czbix.v2ex.util.UserUtils;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.eventbus.Subscribe;


public class MainActivity extends BaseActivity implements OnTopicActionListener,
        NavigationView.OnNavigationItemSelectedListener, NodeListFragment.OnNodeActionListener, FragmentManager.OnBackStackChangedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PREF_DRAWER_SHOWED = "drawer_showed";
    public static final String GOTO_NOTIFICATIONS = "notifications";

    public static final String BUNDLE_NODE = "node";
    public static final String BUNDLE_GOTO = "goto";

    private TextView mUsername;
    private AppBarLayout mAppBar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNav;
    private ImageView mAvatar;
    private SharedPreferences mPreferences;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private View mNavBg;
    private MenuItem mNotificationsItem;
    private View mAwardButton;
    private SearchBoxLayout mSearchBox;
    private TopicSearchPresenter mSearchPresenter;
    @IdRes
    private int mLastMenuId;
    private MenuItem mSearchMenuItem;
    private boolean mIsCategoryTabFragment;

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
        mSearchBox = (SearchBoxLayout) findViewById(R.id.search_box);
        mNavBg = mNav.findViewById(R.id.nav_layout);

        initToolbar();
        initNavDrawer();
        initSearchBox();

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        switchFragment(getFragmentToShow(getIntent()), false);
    }

    private void initSearchBox() {
        mSearchPresenter = new TopicSearchPresenter(this);
        mSearchBox.setOnActionListener(new SearchBoxLayout.Listener() {
            @Override
            public void onQueryTextChange(String newText) {
                mSearchPresenter.changeQuery(newText);
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                final boolean result = mSearchPresenter.submitQuery(query);
                return result;
            }

            @Override
            public void start() {
                mSearchPresenter.start();
            }

            @Override
            public void end() {
                mSearchPresenter.end();
            }
        });
    }

    private Fragment getFragmentToShow(Intent intent) {
        Node node = null;
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            final String url = intent.getDataString();
            final String name = Node.getNameFromUrl(url);
            node = new Node.Builder().setName(name).createNode();
        } else if (intent.hasExtra(BUNDLE_NODE)) {
            node = intent.getParcelableExtra(BUNDLE_NODE);
        } else if (intent.hasExtra(BUNDLE_GOTO)) {
            final String dest = intent.getStringExtra(BUNDLE_GOTO);
            switch (dest) {
                case GOTO_NOTIFICATIONS:
                    return NotificationListFragment.newInstance();
                default:
                    throw new IllegalArgumentException("unknown goto dest: " + dest);
            }
        }

        if (node != null) {
            return TopicListFragment.newInstance(node);
        }

        return CategoryTabFragment.newInstance();
    }

    public void setAppBarShadow(boolean isShown) {
        final int elevation = isShown
                ? getResources().getDimensionPixelSize(R.dimen.appbar_elevation) : 0;
        ViewCompat.setElevation(mAppBar, elevation);
    }

    @Override
    protected void onStart() {
        super.onStart();

        AppCtx.getEventBus().register(this);

        supportInvalidateOptionsMenu();
        updateUsername();
        updateNavBackground();
        updateNotifications();
        setAwardVisibility(UserState.getInstance().hasAward());
    }

    private void updateNotifications() {
        boolean isEnable;
        int iconId;
        int titleId;
        if (!UserState.getInstance().isLoggedIn()) {
            isEnable = false;
            iconId = R.drawable.ic_notifications_none_black_24dp;
            titleId = R.string.drawer_notifications;
        } else if (UserState.getInstance().hasUnread()) {
            isEnable = true;
            iconId = R.drawable.ic_notifications_black_24dp;
            titleId = R.string.drawer_unread_notifications;
        } else {
            isEnable = true;
            iconId = R.drawable.ic_notifications_none_black_24dp;
            titleId = R.string.drawer_notifications;
        }

        mNotificationsItem.setEnabled(isEnable);
        mNotificationsItem.setIcon(iconId);
        mNotificationsItem.setTitle(titleId);
    }

    private void updateNavBackground() {
        String url = GoogleImg.ALL_LOCATION[GoogleImg.getLocationIndex()][GoogleImg.getTimeIndex()];
        Glide.with(this).using(GooglePhotoUrlLoader.getInstance()).load(url)
                .crossFade().centerCrop().into(new ViewTarget<View, GlideDrawable>(mNavBg) {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                resource.setColorFilter(new LightingColorFilter(Color.rgb(180, 180, 180), 0));
                ViewUtils.setBackground(mNavBg, resource);
            }
        });
    }

    @Subscribe
    public void onDailyMissionEvent(DailyAwardEvent e) {
        if (!e.mHasAward) {
            Toast.makeText(this, R.string.toast_daily_award_received, Toast.LENGTH_LONG).show();
        }
        setAwardVisibility(e.mHasAward);
        mAwardButton.setEnabled(true);
    }

    private void setAwardVisibility(boolean visibility) {
        mAwardButton.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
    }

    private void initNavDrawer() {
        mLastMenuId = R.id.drawer_explore;
        mNav.setNavigationItemSelectedListener(this);
        if (!mPreferences.getBoolean(PREF_DRAWER_SHOWED, false)) {
            mDrawerLayout.openDrawer(mNav);
            mPreferences.edit().putBoolean(PREF_DRAWER_SHOWED, true).apply();
        }
        final Menu menu = mNav.getMenu();
        mNotificationsItem = menu.findItem(R.id.drawer_notifications);
        updateNotifications();

        mAwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAwardButton.setEnabled(false);
                ExecutorUtils.execute(new Runnable() {
                    @Override
                    public void run() {
                        boolean success = false;
                        try {
                            RequestHelper.dailyMission();
                            success = true;
                        } catch (ConnectionException | RemoteException e) {
                            LogUtils.w(TAG, "daily mission failed", e);
                        }

                        AppCtx.getEventBus().post(new DailyAwardEvent(!success));
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
                final UserState user = UserState.getInstance();
                if (user.isLoggedIn()) {
                    MiscUtils.openUrl(MainActivity.this, Member.buildUrlFromName(user.getUsername()));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        final Fragment fragment = getFragmentToShow(intent);
        mDrawerLayout.closeDrawer(mNav);
        switchFragment(fragment, true);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == mLastMenuId) {
            return false;
        }

        switch (itemId) {
            case R.id.drawer_explore:
                mLastMenuId = R.id.drawer_explore;
                mDrawerLayout.closeDrawer(mNav);
                switchFragment(CategoryTabFragment.newInstance());
                return true;
            case R.id.drawer_nodes:
                mLastMenuId = R.id.drawer_nodes;
                mDrawerLayout.closeDrawer(mNav);
                switchFragment(NodeListFragment.newInstance());
                return true;
            case R.id.drawer_notifications:
                mLastMenuId = R.id.drawer_notifications;
                mDrawerLayout.closeDrawer(mNav);
                switchFragment(NotificationListFragment.newInstance());
                return true;
            case R.id.drawer_favorite:
                mLastMenuId = R.id.drawer_favorite;
                mDrawerLayout.closeDrawer(mNav);
                switchFragment(TopicListFragment.newInstance(Page.PAGE_FAV_TOPIC));
                return true;
            case R.id.drawer_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.drawer_feedback:
                final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"gliuwr+gp@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, String.format("V2EX(%s) feedback",
                        BuildConfig.VERSION_NAME));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.toast_email_app_not_found, Toast.LENGTH_SHORT);
                }
                return true;
            case R.id.drawer_about:
                Toast.makeText(this, getString(R.string.toast_app_version, BuildConfig.VERSION_NAME),
                        Toast.LENGTH_SHORT).show();
                return true;
        }

        return false;
    }

    public void setNavSelected(@IdRes int menuId) {
        mLastMenuId = menuId;
        mNav.setCheckedItem(menuId);
    }

    private void switchFragment(Fragment fragment) {
        switchFragment(fragment, true);
    }

    private void switchFragment(Fragment fragment, boolean addToBackStack) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out,
                R.anim.abc_fade_in, R.anim.abc_fade_out)
                .replace(R.id.fragment, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();

        mIsCategoryTabFragment = isCategoryTabFragment(fragment);
        setAppBarShadow(!mIsCategoryTabFragment);
        invalidateOptionsMenu();
    }

    private void updateUsername() {
        if (!UserState.getInstance().isLoggedIn()) {
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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mSearchMenuItem = menu.findItem(R.id.action_web_search);
        mSearchMenuItem.setVisible(mIsCategoryTabFragment);

        enableLoginMenu(menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mSearchMenuItem) {
            TrackerUtils.onSearch();
            mSearchBox.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableLoginMenu(Menu menu) {
        if (UserState.getInstance().isLoggedIn()) {
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
        supportInvalidateOptionsMenu();

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

        if (mSearchBox.getVisibility() == View.VISIBLE) {
            mSearchBox.hide(false);
        }

        AppCtx.getEventBus().unregister(this);
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
    public boolean onTopicOpen(View view, Topic topic) {
        final Intent intent = new Intent(this, TopicActivity.class);
        intent.putExtra(TopicActivity.KEY_TOPIC, topic);

        startActivity(intent);

        TopicDao.updateLastRead(topic);
        if (topic.getReplyCount() > 0) {
            topic.setHasRead();
            return true;
        }

        return false;
    }

    @Override
    public void onNodeOpen(Node node) {
        final TopicListFragment topicListFragment = TopicListFragment.newInstance(node);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, topicListFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackStackChanged() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        setAppBarShadow(!isCategoryTabFragment(fragment));
    }

    private static boolean isCategoryTabFragment(Fragment fragment) {
        return fragment instanceof CategoryTabFragment;
    }
}
