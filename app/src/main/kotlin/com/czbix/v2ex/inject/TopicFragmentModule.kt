package com.czbix.v2ex.inject

import androidx.lifecycle.ViewModel
import com.czbix.v2ex.ui.fragment.TopicFragment
import com.czbix.v2ex.ui.model.TopicViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class TopicFragmentModule {
    @Binds
    @IntoMap
    @ViewModelKey(TopicViewModel::class)
    abstract fun bindTopicViewModel(viewModel: TopicViewModel): ViewModel

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun topicFragment(): TopicFragment
}