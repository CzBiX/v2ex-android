package com.czbix.v2ex.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.czbix.v2ex.ui.model.NightModeViewModel
import com.czbix.v2ex.ui.settings.PrefsFragment
import javax.inject.Inject

class SettingsActivity : BaseActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val nightModeViewModel: NightModeViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initNightMode(nightModeViewModel.nightMode)

        supportFragmentManager.beginTransaction().replace(android.R.id.content, PrefsFragment()).commit()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        mFragment.onActivityResult(requestCode, resultCode, data)
//    }
}
