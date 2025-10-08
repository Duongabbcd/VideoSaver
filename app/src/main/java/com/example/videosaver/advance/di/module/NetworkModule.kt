package com.example.videosaver.advance.di.module

import android.app.Application
import com.example.videosaver.advance.data.remote.service.ConfigService
import com.example.videosaver.advance.data.remote.service.VideoService
import com.example.videosaver.advance.data.remote.service.VideoServiceLocal
import com.example.videosaver.utils.advance.proxy_utils.CustomProxyController
import com.example.videosaver.utils.advance.util.Memory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    companion object {
        private const val DATA_URL = "https://some-url.com/youtube-dl/"
    }

    @Provides
    @Singleton
    fun buildOkHttpClient(application: Application): OkHttpClient =
        OkHttpClient.Builder().retryOnConnectionFailure(true)
            .connectTimeout(10L, TimeUnit.SECONDS)
            .writeTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .cache(
                Cache(
                    File(application.cacheDir, "YoutubeDLCache"),
                    Memory.calcCacheSize(application, .25f)
                )
            )
            .build()

    @Provides
    @Singleton
    @Named("AdvanceClient")
    fun provideOkHttpClient(application: Application): OkHttpClient = buildOkHttpClient(application)

    @Provides
    @Singleton
    fun provideConfigService(@Named("AdvanceClient") okHttpClient: OkHttpClient): ConfigService = Retrofit.Builder()
        .baseUrl(DATA_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
        .create(ConfigService::class.java)

    @Provides
    @Singleton
    fun provideVideoService(
        proxyController: CustomProxyController,
    ): VideoService =
        VideoServiceLocal(
            proxyController
        )
}
