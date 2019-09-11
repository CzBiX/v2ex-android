package com.czbix.v2ex.inject

import com.czbix.v2ex.ui.MainActivity
import com.czbix.v2ex.ui.TopicActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ActivityModule {
    @ActivityScoped
    @ContributesAndroidInjector(modules = [MainFragmentModule::class])
    abstract fun mainActivity(): MainActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [TopicFragmentModule::class])
    abstract fun topicActivity(): TopicActivity
}