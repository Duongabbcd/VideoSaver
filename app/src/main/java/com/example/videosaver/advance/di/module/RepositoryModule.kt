package com.example.videosaver.advance.di.module

import com.example.videosaver.advance.data.local.TopPagesLocalDataSource
import com.example.videosaver.advance.data.remote.TopPagesRemoteDataSource
import com.example.videosaver.advance.data.repository.TopPagesRepository
import com.example.videosaver.advance.data.repository.TopPagesRepositoryImpl
import com.example.videosaver.advance.di.qualifier.LocalData
import com.example.videosaver.advance.di.qualifier.RemoteData
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // region ConfigRepository
//    @Binds
//    @Singleton
//    @LocalData
//    abstract fun bindConfigLocal(local: ConfigLocalDataSource): ConfigRepository
//
//    @Binds
//    @Singleton
//    @RemoteData
//    abstract fun bindConfigRemote(remote: ConfigRemoteDataSource): ConfigRepository
//
//    @Binds
//    @Singleton
//    abstract fun bindConfigRepository(impl: ConfigRepositoryImpl): ConfigRepository

    // endregion

    // region TopPagesRepository
    @Binds
    @Singleton
    @LocalData
    abstract fun bindTopPagesLocal(local: TopPagesLocalDataSource): TopPagesRepository

    @Binds
    @Singleton
    @RemoteData
    abstract fun bindTopPagesRemote(remote: TopPagesRemoteDataSource): TopPagesRepository

    @Binds
    @Singleton
    abstract fun bindTopPagesRepository(impl: TopPagesRepositoryImpl): TopPagesRepository
    // endregion

    // ... same pattern for VideoRepository, HistoryRepository, etc.
}