package com.lifelab

import android.app.Application
import com.lifelab.core.database.LifeLabDatabase
import com.lifelab.feature.experiment.data.repository.ExperimentRepository

class LifeLabApplication : Application() {
    val database : LifeLabDatabase by lazy {
        LifeLabDatabase.getInstance(this)
    }

    val experimentRepository : ExperimentRepository by lazy {
        ExperimentRepository(database.experimentDao())
    }
}