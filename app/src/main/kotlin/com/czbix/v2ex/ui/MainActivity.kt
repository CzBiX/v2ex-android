package com.czbix.v2ex.ui

import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.graphics.Outline
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.AppBarLayout
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v4.view.ViewCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.ViewTarget
import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.BuildConfig
import com.czbix.v2ex.R
import com.czbix.v2ex.common.NotificationStatus
import com.czbix.v2ex.common.UpdateInfo
import com.czbix.v2ex.common.UserState
import com.czbix.v2ex.event.AppUpdateEvent
import com.czbix.v2ex.event.BaseEvent.DailyAwardEvent
import com.czbix.v2ex.event.BaseEvent.NewUnreadEvent
import com.czbix.v2ex.eventbus.LoginEvent
import com.czbix.v2ex.helper.RxBus
import com.czbix.v2ex.model.Member
import com.czbix.v2ex.model.Node
import com.czbix.v2ex.model.loader.GooglePhotoUrlLoader
import com.czbix.v2ex.network.RequestHelper
import com.czbix.v2ex.presenter.TopicSearchPresenter
import com.czbix.v2ex.res.GoogleImg
import com.czbix.v2ex.ui.fragment.*
import com.czbix.v2ex.ui.widget.SearchBoxLayout
import com.czbix.v2ex.util.*
import com.google.common.eventbus.Subscribe
import io.reactivex.disposables.Disposable

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
        NodeListFragment.OnNodeActionListener, FragmentManager.OnBackStackChangedListener {
    private var mIsTabFragment: Boolean = false
    private var mToolbar: Toolbar? = null
    private val disposables: MutableList<Disposable> = mutableListOf()
    private var hasAward: Boolean = false
    private lateinit var mUsername: TextView
    private lateinit var mAppBar: AppBarLayout
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mNav: NavigationView
    private lateinit var mAvatar: ImageView
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private lateinit var mNavBg: View
    private lateinit var mNotificationsItem: MenuItem
    private lateinit var mUpdateItem: MenuItem
    private lateinit var mAwardButton: View
    private lateinit var mSearchPresenter: TopicSearchPresenter
    private lateinit var mSearchMenuItem: MenuItem
    private lateinit var mFavItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAppBar = findViewById(R.id.appbar)
        mDrawerLayout = findViewById(R.id.drawer_layout)

        mNav = findViewById(R.id.nav)

        val headerView = mNav.getHeaderView(0)
        mNavBg = headerView.findViewById(R.id.nav_layout)
        mAvatar = headerView.findViewById(R.id.avatar_img)
        mUsername = headerView.findViewById(R.id.username_tv)
        mAwardButton = headerView.findViewById(R.id.award)
        val searchBox = findViewById<SearchBoxLayout>(R.id.search_box)
        mSearchPresenter = TopicSearchPresenter(this, searchBox)

        mToolbar = ViewUtils.initToolbar(this)
        initNavDrawer()

        supportFragmentManager.addOnBackStackChangedListener(this)
        switchFragment(getFragmentToShow(intent), false)
    }

    override fun onDestroy() {
        super.onDestroy()

        disposables.dispose()
    }

    private fun onAppUpdateEvent() {
        mUpdateItem.isVisible = true
        if (UpdateInfo.isRecommend) {
            NotificationStatus.showAppUpdate()
        }
    }

    private fun getFragmentToShow(intent: Intent): Fragment {
        var node: Node? = null
        if (Intent.ACTION_VIEW == intent.action) {
            val url = intent.dataString
            val name = Node.getNameFromUrl(url)
            node = Node.Builder().setName(name).createNode()
        } else if (intent.hasExtra(BUNDLE_NODE)) {
            node = intent.getParcelableExtra<Node>(BUNDLE_NODE)
        } else if (intent.hasExtra(BUNDLE_GOTO)) {
            val dest = intent.getStringExtra(BUNDLE_GOTO)
            when (dest) {
                GOTO_NOTIFICATIONS -> return NotificationListFragment.newInstance()
                else -> throw IllegalArgumentException("unknown goto dest: " + dest)
            }
        }

        if (node != null) {
            return TopicListFragment.newInstance(node)
        }

        return CategoryTabFragment.newInstance()
    }

    fun setAppBarShadow(isShown: Boolean) {
        val elevation = if (isShown) {
            resources.getDimensionPixelSize(R.dimen.appbar_elevation)
        } else {
            0
        }
        ViewCompat.setElevation(mAppBar, elevation.toFloat())
    }

    override fun onStart() {
        super.onStart()

        AppCtx.eventBus.register(this)

        invalidateOptionsMenu()
        updateUsername()
        updateNavBackground()
        updateNotifications()
        updateFavItem()
        setAwardVisibility(UserState.hasAward())
    }

    private fun updateNotifications() {
        val isEnable: Boolean
        val iconId: Int
        val titleId: Int
        if (!UserState.isLoggedIn()) {
            isEnable = false
            iconId = R.drawable.ic_notifications_none_black_24dp
            titleId = R.string.drawer_notifications
        } else if (UserState.hasUnread()) {
            isEnable = true
            iconId = R.drawable.ic_notifications_black_24dp
            titleId = R.string.drawer_unread_notifications
        } else {
            isEnable = true
            iconId = R.drawable.ic_notifications_none_black_24dp
            titleId = R.string.drawer_notifications
        }

        mNotificationsItem.isEnabled = isEnable
        mNotificationsItem.setIcon(iconId)
        mNotificationsItem.setTitle(titleId)
    }

    private fun updateFavItem() {
        mFavItem.isEnabled = UserState.isLoggedIn()
    }

    private fun updateNavBackground() {
        val url = GoogleImg.ALL_LOCATION[GoogleImg.getRandomLocationIndex()][GoogleImg.getCurrentTimeIndex()]
        Glide.with(this).using(GooglePhotoUrlLoader.getInstance()).load(url).crossFade().centerCrop().into(object : ViewTarget<View, GlideDrawable>(mNavBg) {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onResourceReady(resource: GlideDrawable, glideAnimation: GlideAnimation<in GlideDrawable>) {
                resource.colorFilter = LightingColorFilter(Color.rgb(180, 180, 180), 0)
                ViewUtils.setBackground(mNavBg, resource)
            }
        })
    }

    fun onDailyMissionEvent(e: DailyAwardEvent) {
        if (!e.mHasAward && hasAward) {
            Toast.makeText(this, R.string.toast_daily_award_received, Toast.LENGTH_LONG).show()
        }

        hasAward = e.mHasAward
        setAwardVisibility(e.mHasAward)
        mAwardButton.isEnabled = true
    }

    private fun setAwardVisibility(visibility: Boolean) {
        mAwardButton.visibility = if (visibility) View.VISIBLE else View.INVISIBLE
    }

    private fun initNavDrawer() {
        getPreferences(Context.MODE_PRIVATE).let { it ->
            val prefs = it!!

            if (!prefs.getBoolean(PREF_DRAWER_SHOWED, false)) {
                mDrawerLayout.openDrawer(mNav)
                prefs.edit().putBoolean(PREF_DRAWER_SHOWED, true).apply()
            }
        }

        mNav.setNavigationItemSelectedListener(this)

        val menu = mNav.menu
        mNotificationsItem = menu.findItem(R.id.drawer_notifications)
        mFavItem = menu.findItem(R.id.drawer_favorite)
        mUpdateItem = menu.findItem(R.id.drawer_update)

        updateNotifications()
        updateFavItem()

        mAwardButton.setOnClickListener {
            mAwardButton.isEnabled = false

            RequestHelper.dailyBonus().await({
                RxBus.post(DailyAwardEvent(false))
            }, { e ->
                LogUtils.w(TAG, "Get daily bonus failed", e)

                RxBus.post(DailyAwardEvent(true))
            })
        }

        mDrawerToggle = ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.desc_open_drawer, R.string.desc_close_drawer)
        mDrawerLayout.addDrawerListener(mDrawerToggle)

        mNavBg.setOnClickListener {
            if (UserState.isLoggedIn()) {
                MiscUtils.openUrl(this@MainActivity, Member.buildUrlFromName(UserState.username!!))
            } else {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
        }

        if (MiscUtils.HAS_L) {
            setAvatarOutline()
            setStatusBarTransparent()
        }

        if (UpdateInfo.hasNewVersion) {
            onAppUpdateEvent()
        } else {
            disposables += RxBus.subscribe<AppUpdateEvent> {
                onAppUpdateEvent()
            }
        }

        disposables += RxBus.subscribe<NewUnreadEvent> {
            updateNotifications()
        }

        disposables += RxBus.subscribe<DailyAwardEvent> {
            onDailyMissionEvent(it)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setStatusBarTransparent() {
        window.statusBarColor = Color.TRANSPARENT
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setAvatarOutline() {
        mAvatar.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setOval(0, 0, view.width - 1, view.height - 1)
            }
        }
        mAvatar.clipToOutline = true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mDrawerToggle.syncState()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val fragment = getFragmentToShow(intent)
        mDrawerLayout.closeDrawer(mNav)
        switchFragment(fragment, true)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.isChecked) {
            mDrawerLayout.closeDrawer(mNav)
            return true
        }

        val handled = when (item.itemId) {
            R.id.drawer_explore -> {
                switchFragment(CategoryTabFragment.newInstance())
                true
            }
            R.id.drawer_nodes -> {
                switchFragment(NodeListFragment.newInstance())
                true
            }
            R.id.drawer_notifications -> {
                switchFragment(NotificationListFragment.newInstance())
                true
            }
            R.id.drawer_favorite -> {
                switchFragment(FavoriteTabFragment.newInstance())
                true
            }
            R.id.drawer_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.drawer_update -> {
                startActivity(MiscUtils.appUpdateIntent)
                true
            }
            R.id.drawer_feedback -> {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("googleplay@czbix.com"))
                    putExtra(Intent.EXTRA_SUBJECT, String.format("V2EX(%s) feedback",
                            BuildConfig.VERSION_NAME))
                }

                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, R.string.toast_email_app_not_found, Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> false
        }

        if (handled) {
            mDrawerLayout.closeDrawer(mNav)
        }

        return handled
    }

    fun setNavSelected(@IdRes menuId: Int) {
        mNav.setCheckedItem(menuId)
    }

    private fun switchFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(0, 0, R.anim.abc_fade_in, R.anim.abc_fade_out).replace(R.id.fragment, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()

        mIsTabFragment = isTabFragment(fragment)
        setAppBarShadow(!mIsTabFragment)
        invalidateOptionsMenu()
    }

    private fun updateUsername() {
        if (!UserState.isLoggedIn()) {
            mAvatar.visibility = View.INVISIBLE
            mUsername.setText(R.string.action_sign_in)
            return
        }

        mAvatar.visibility = View.VISIBLE
        val avatar = UserUtils.getAvatar()
        val request = Glide.with(this).load(avatar.getUrlByDp(resources.getDimension(R.dimen.nav_avatar_size)))
        if (MiscUtils.HAS_L) {
            request.crossFade().into(mAvatar)
        } else {
            // crop bitmap manually
            request.asBitmap().into(object : ViewTarget<ImageView, Bitmap>(mAvatar) {
                override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>) {
                    val drawable = RoundedBitmapDrawableFactory.create(resources, resource)
                    drawable.isCircular = true
                    mAvatar.setImageDrawable(drawable)
                }
            })
        }
        mUsername.text = UserState.username
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        mSearchMenuItem = menu.findItem(R.id.action_web_search)
        mSearchMenuItem.isVisible = mIsTabFragment

        enableLoginMenu(menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item === mSearchMenuItem) {
            TrackerUtils.onSearch()
            mSearchPresenter.show()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun enableLoginMenu(menu: Menu) {
        if (UserState.isLoggedIn()) {
            return
        }

        val loginMenu = menu.add(R.string.action_sign_in)
        loginMenu.setOnMenuItemClickListener {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            true
        }
    }

    @Subscribe
    fun onLoginEvent(e: LoginEvent) {
        invalidateOptionsMenu()

        updateUsername()
        updateNotifications()
        updateFavItem()
    }

    override fun onStop() {
        super.onStop()

        mSearchPresenter.hide(false)

        AppCtx.eventBus.unregister(this)
    }

    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNav)) {
            mDrawerLayout.closeDrawer(mNav)
            return
        }

        if (mSearchPresenter.isVisible()) {
            mSearchPresenter.hide()
            return
        }

        super.onBackPressed()
    }

    override fun onNodeOpen(node: Node) {
        val topicListFragment = TopicListFragment.newInstance(node)

        supportFragmentManager.beginTransaction().replace(R.id.fragment, topicListFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit()
    }

    override fun onBackStackChanged() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment)
        setAppBarShadow(!isTabFragment(fragment))
    }

    companion object {
        private val TAG = getLogTag<MainActivity>()
        private const val PREF_DRAWER_SHOWED = "drawer_showed"

        const val GOTO_NOTIFICATIONS = "notifications"

        const val BUNDLE_NODE = "node"
        const val BUNDLE_GOTO = "goto"

        private fun isTabFragment(fragment: Fragment): Boolean {
            return fragment is BaseTabFragment
        }
    }
}
