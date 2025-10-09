package com.example.videosaver.advance.di.module

import com.example.videosaver.advance.data.local.HistoryLocalDataSource
import com.example.videosaver.advance.data.local.ProgressLocalDataSource
import com.example.videosaver.advance.data.local.TopPagesLocalDataSource
import com.example.videosaver.advance.data.local.VideoLocalDataSource
import com.example.videosaver.advance.data.remote.TopPagesRemoteDataSource
import com.example.videosaver.advance.data.remote.VideoRemoteDataSource
import com.example.videosaver.advance.data.repository.HistoryRepository
import com.example.videosaver.advance.data.repository.HistoryRepositoryImpl
import com.example.videosaver.advance.data.repository.ProgressRepository
import com.example.videosaver.advance.data.repository.ProgressRepositoryImpl
import com.example.videosaver.advance.data.repository.TopPagesRepository
import com.example.videosaver.advance.data.repository.TopPagesRepositoryImpl
import com.example.videosaver.advance.data.repository.VideoRepository
import com.example.videosaver.advance.data.repository.VideoRepositoryImpl
import com.example.videosaver.advance.di.qualifier.LocalData
import com.example.videosaver.advance.di.qualifier.RemoteData
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
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

    @Singleton
    @Binds
    @LocalData
    abstract fun bindVideoLocalDataSource(localDataSource: VideoLocalDataSource): VideoRepository

    @Singleton
    @Binds
    @RemoteData
    abstract fun bindVideoRemoteDataSource(remoteDataSource: VideoRemoteDataSource): VideoRepository

    @Singleton
    @Binds
    abstract fun bindVideoRepositoryImpl(videoRepository: VideoRepositoryImpl): VideoRepository


    @Singleton
    @Binds
    @LocalData
    abstract fun bindProgressLocalDataSource(localDataSource: ProgressLocalDataSource): ProgressRepository


    @Singleton
    @Binds
    abstract fun bindProgressRepositoryImpl(progressRepository: ProgressRepositoryImpl): ProgressRepository


    @Singleton
    @Binds
    abstract fun bindHistoryRepositoryImpl(historyRepository: HistoryRepositoryImpl): HistoryRepository

    @Singleton
    @Binds
    @LocalData
    abstract fun bindHistoryLocalDataSource(localDataSource: HistoryLocalDataSource): HistoryRepository
}