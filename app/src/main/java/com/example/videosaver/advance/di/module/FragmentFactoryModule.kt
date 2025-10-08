package com.example.videosaver.advance.di.module

import com.example.videosaver.utils.advance.fragment.FragmentFactory
import com.example.videosaver.utils.advance.fragment.FragmentFactoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FragmentFactoryModule {

    @Binds
    @Singleton // Optional if you want a singleton instance
    abstract fun bindFragmentFactory(
        impl: FragmentFactoryImpl
    ): FragmentFactory
}