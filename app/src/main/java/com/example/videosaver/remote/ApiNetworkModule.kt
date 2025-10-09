package com.example.videosaver.remote

import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
object ApiNetworkModule {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    @Named("RemoteClient")
    fun provideOkHttpClient(@Named("APIFY_TOKEN") token: String): OkHttpClient {
        val interceptor = Interceptor { chain ->
            val original: Request = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer $token") // standard Bearer header
            chain.proceed(requestBuilder.build())
        }
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Named("APIFY_TOKEN")
    fun provideApifyToken() = ""

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApifyService =
        retrofit.create(ApifyService::class.java)

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.apify.com/") // Make sure this matches your actual API base
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}