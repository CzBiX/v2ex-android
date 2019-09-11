package com.czbix.v2ex.inject


import androidx.lifecycle.ViewModelProvider
import com.czbix.v2ex.model.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}