package com.czbix.v2ex.presenter

import android.app.Activity
import android.net.Uri
import android.text.TextUtils
import android.view.View
import com.czbix.v2ex.helper.CustomTabsHelper
import com.czbix.v2ex.ui.widget.SearchBoxLayout
import com.czbix.v2ex.util.ExecutorUtils
import com.google.common.base.Strings
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class TopicSearchPresenter(private val mActivity: Activity, val searchBox: SearchBoxLayout) {
    private val mCustomTabsHelper: CustomTabsHelper
    private var mPrefetchTask: ScheduledFuture<*>? = null

    init {
        mCustomTabsHelper = CustomTabsHelper()
        searchBox.setOnActionListener(object : SearchBoxLayout.Listener {
            override fun onQueryTextChange(newText: String) {
                changeQuery(newText)
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                val result = submitQuery(query)
                return result
            }

            override fun onShow() {
                start()
            }

            override fun onHide() {
                end()
            }
        })
    }

    fun start() {
        mCustomTabsHelper.bindCustomTabsService(mActivity)
    }

    fun end() {
        mCustomTabsHelper.unbindCustomTabsService(mActivity)
    }

    private fun changeQuery(query: String) {
        if (Strings.isNullOrEmpty(query)) {
            return
        }

        mPrefetchTask?.cancel(true)

        if (!query.endsWith(" ")) {
            mPrefetchTask = ExecutorUtils.schedule(DELAY_BEFORE_PREFETCH, TimeUnit.MILLISECONDS) {
                mCustomTabsHelper.mayLaunchUrl(getSearchUri(query), null, null)
            }
        }
    }

    private fun submitQuery(query: String): Boolean {
        if (TextUtils.isEmpty(query)) {
            return false
        }

        mPrefetchTask?.cancel(true)

        openSearchActivity(query)
        return true
    }

    private fun getSearchUri(query: String): Uri {
        val queryToSearch = query + " site:https://www.v2ex.com"

        return Uri.Builder().scheme("https").authority("duckduckgo.com")
                .appendQueryParameter("q", queryToSearch).build()
    }

    private fun openSearchActivity(query: String) {
        val uri = getSearchUri(query)

        val intent = CustomTabsHelper.getBuilder(mActivity,
                mCustomTabsHelper.session).build()
        intent.launchUrl(mActivity, uri)
    }

    fun show() {
        searchBox.show()
    }

    fun hide(withAnimation: Boolean = true) {
        searchBox.hide(withAnimation)
    }

    fun isVisible(): Boolean {
        return searchBox.visibility == View.VISIBLE
    }

    companion object {
        private val DELAY_BEFORE_PREFETCH = 400L
    }
}
