package com.lifelab

import android.app.Application
import com.lifelab.core.database.LifeLabDatabase
import com.lifelab.core.network.NetworkModule
import com.lifelab.core.session.AuthTokenStore
import com.lifelab.feature.auth.data.repository.AuthRepository
import com.lifelab.feature.experiment.data.repository.ExperimentRepository
import com.tencent.mmkv.MMKV

class LifeLabApplication : Application() {
    lateinit var authTokenStore: AuthTokenStore
        private set

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            NetworkModule.authApi,
            authTokenStore
        )
    }

    val database : LifeLabDatabase by lazy {
        LifeLabDatabase.getInstance(this)
    }

    val experimentRepository : ExperimentRepository by lazy {
        ExperimentRepository(database.experimentDao())
    }
    override fun onCreate() {
        super.onCreate()

        MMKV.initialize(this)

        authTokenStore = AuthTokenStore()

        NetworkModule.initialize(authTokenStore)
    }
}