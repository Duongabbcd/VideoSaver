package com.example.videosaver.advance.di.component

import com.example.videosaver.MyApplication
import com.example.videosaver.advance.di.module.ActivityBindingModule
import com.example.videosaver.advance.di.module.AppModule
import com.example.videosaver.advance.di.module.DatabaseModule
import com.example.videosaver.advance.di.module.FirebaseModule
import com.example.videosaver.advance.di.module.MyWorkerModule
import com.example.videosaver.advance.di.module.RepositoryModule
import com.example.videosaver.advance.di.module.UtilModule
import com.example.videosaver.advance.di.module.ViewModelModule
import com.example.videosaver.advance.di.module.NetworkModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AndroidSupportInjectionModule::class, AppModule::class, ActivityBindingModule::class, UtilModule::class,
        DatabaseModule::class, NetworkModule::class, RepositoryModule::class, ViewModelModule::class, MyWorkerModule::class, FirebaseModule::class]
)
interface AppComponent : AndroidInjector<MyApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: MyApplication): Builder

        fun build(): AppComponent
    }
}