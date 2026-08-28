package com.lifelab

import android.app.Application
import com.lifelab.core.database.LifeLabDatabase
import com.lifelab.core.network.NetworkModule
import com.lifelab.core.session.AuthTokenStore
import com.lifelab.core.sync.data.repository.DataSyncRepository
import com.lifelab.feature.auth.data.repository.AuthRepository
import com.lifelab.feature.experiment.data.repository.ExperimentRepository
import com.lifelab.feature.record.data.repository.RecordRepository
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
    val recordRepository: RecordRepository by lazy {
        RecordRepository(
            recordDao = database.recordDao(),
            context = applicationContext,
        )
    }
    val database: LifeLabDatabase by lazy {
        LifeLabDatabase.getInstance(this)
    }

    val dataSyncRepository: DataSyncRepository by lazy {
        DataSyncRepository(
            syncApi = NetworkModule.syncApi,
            database = database,
            authTokenStore = authTokenStore,
        )
    }

    val experimentRepository: ExperimentRepository by lazy {
        ExperimentRepository(
            database.experimentDao(),
            context = applicationContext,
            syncApi = NetworkModule.syncApi,
        )
    }

    override fun onCreate() {
        super.onCreate()

        MMKV.initialize(this)

        authTokenStore = AuthTokenStore()

        NetworkModule.initialize(authTokenStore)
    }
}
