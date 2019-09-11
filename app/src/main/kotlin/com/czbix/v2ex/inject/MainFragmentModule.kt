package com.czbix.v2ex.inject

import com.czbix.v2ex.ui.fragment.TopicListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentModule {
    @ContributesAndroidInjector
    abstract fun topicListFragment(): TopicListFragment
}