package com.czbix.v2ex.presenter

import android.app.Activity
import android.net.Uri
import android.text.TextUtils
import com.czbix.v2ex.helper.CustomTabsHelper
import com.czbix.v2ex.util.ExecutorUtils
import com.google.common.base.Strings
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class TopicSearchPresenter(private val mActivity: Activity) {
    private val mCustomTabsHelper: CustomTabsHelper
    private var mPrefetchTask: ScheduledFuture<*>? = null

    init {
        mCustomTabsHelper = CustomTabsHelper()
    }

    fun start() {
        mCustomTabsHelper.bindCustomTabsService(mActivity)
    }

    fun end() {
        mCustomTabsHelper.unbindCustomTabsService(mActivity)
    }

    fun changeQuery(query: String) {
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

    fun submitQuery(query: String): Boolean {
        if (TextUtils.isEmpty(query)) {
            return false
        }

        mPrefetchTask?.cancel(true)

        openSearch(query)
        return true
    }

    private fun getSearchUri(query: String): Uri {
        val queryToSearch = query + " site:https://www.v2ex.com"

        return Uri.Builder().scheme("https").authority("www.google.com").path("/search")
                .appendQueryParameter("q", queryToSearch).build()
    }

    private fun openSearch(query: String) {
        val uri = getSearchUri(query)

        val intent = CustomTabsHelper.getBuilder(mActivity,
                mCustomTabsHelper.session).build()
        intent.launchUrl(mActivity, uri)
    }

    companion object {
        private val DELAY_BEFORE_PREFETCH = 400L
    }
}
