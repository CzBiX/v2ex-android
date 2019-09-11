package com.czbix.v2ex.inject

import androidx.lifecycle.ViewModel
import com.czbix.v2ex.ui.model.NightModeDelegate
import com.czbix.v2ex.ui.model.NightModeDelegateImpl
import com.czbix.v2ex.ui.model.NightModeViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
abstract class NightModeModule {
    @Singleton
    @Binds
    abstract fun provideNightModeDelegate(impl: NightModeDelegateImpl): NightModeDelegate

    @Binds
    @IntoMap
    @ViewModelKey(NightModeViewModel::class)
    abstract fun provideNightModeViewModel(viewModel: NightModeViewModel): ViewModel
}