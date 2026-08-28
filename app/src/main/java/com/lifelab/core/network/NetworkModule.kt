package com.lifelab.core.network

import com.lifelab.BuildConfig
import com.lifelab.core.session.AuthTokenStore
import com.lifelab.core.sync.data.remote.SyncApi
import com.lifelab.feature.auth.data.remote.AuthApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private lateinit var tokenStore: AuthTokenStore

    fun initialize(authTokenStore: AuthTokenStore){
        tokenStore = authTokenStore
    }

    private val authInterceptor: AuthInterceptor by lazy {
        check(::tokenStore.isInitialized){
            "没初始化"
        }
        AuthInterceptor(tokenStore)
    }
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG){
                HttpLoggingInterceptor.Level.BASIC
            }else{
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()
    }
    private val retrofit : Retrofit by lazy {
        Retrofit.Builder().baseUrl(BuildConfig.LIFELAB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
    val syncApi: SyncApi by lazy {
        retrofit.create(SyncApi::class.java)
    }

















}