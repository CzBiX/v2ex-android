package com.czbix.v2ex.inject

import com.czbix.v2ex.AppCtx
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            AndroidInjectionModule::class,
            AppModule::class,
            DbModule::class,
            ViewModelModule::class,
            ActivityModule::class
        ]
)
interface AppComponent : AndroidInjector<AppCtx> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: AppCtx): Builder

        fun build(): AppComponent
    }
}