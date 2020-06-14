package com.czbix.v2ex.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.czbix.v2ex.R
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.ui.fragment.TopicFragment
import com.czbix.v2ex.ui.model.NightModeViewModel
import com.czbix.v2ex.util.ViewUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.common.base.Strings
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class TopicActivity : BaseActivity() {
    var appBarLayout: AppBarLayout? = null
        private set

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: NightModeViewModel by viewModels {
        viewModelFactory
    }

    private fun getTopicFromIntent(): Topic? {
        val intent = intent ?: return null

        if (intent.hasExtra(KEY_TOPIC)) {
            return intent.getParcelableExtra(KEY_TOPIC)
        }
        if (intent.hasExtra(KEY_TOPIC_ID)) {
            val id = intent.getIntExtra(KEY_TOPIC_ID, 0)

            val builder = Topic.Builder()
            builder.id = id
            return builder.build()
        }

        if (intent.action != null && intent.action == Intent.ACTION_VIEW) {
            val url = intent.dataString
            if (!Strings.isNullOrEmpty(url)) {
                val id: Int
                try {
                    id = Topic.getIdFromUrl(url!!)
                } catch (e: IllegalArgumentException) {
                    FirebaseCrashlytics.getInstance().log("unsupported url: " + url!!)
                    Toast.makeText(this, R.string.toast_unsupported_url, Toast.LENGTH_LONG).show()

                    return null
                }

                val builder = Topic.Builder()
                builder.id = id
                return builder.build()

            }
        }

        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initNightMode(viewModel.nightMode)

        setContentView(R.layout.activity_topic)
        ViewUtils.initToolbar(this)
        appBarLayout = findViewById(R.id.appbar)

        setupTransparentNavigationBar()

        if (savedInstanceState == null) {
            val topic = getTopicFromIntent()
            if (topic == null) {
                finish()
                return
            }

            addFragmentToView(topic)
        }
    }

    private fun addFragmentToView(topic: Topic) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, TopicFragment.newInstance(topic))
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private val TAG = TopicActivity::class.java.simpleName

        const val KEY_TOPIC = "topic"
        const val KEY_TOPIC_ID = "topic_id"
    }
}
