package com.czbix.v2ex.ui.settings

import androidx.lifecycle.ViewModel
import com.czbix.v2ex.inject.FragmentScoped
import com.czbix.v2ex.inject.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class SettingsModule {
    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun prefsFragment(): PrefsFragment

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel
}