package com.lifelab.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lifelab.feature.experiment.data.local.dao.ExperimentDao
import com.lifelab.feature.experiment.data.local.entity.ExperimentEntity
import com.lifelab.feature.experiment.data.local.entity.MetricEntity

@Database(
    entities = [
        ExperimentEntity::class,
        MetricEntity::class,
    ],
    version = 1,
    exportSchema = false,
)

abstract class LifeLabDatabase : RoomDatabase(){

    abstract fun experimentDao() : ExperimentDao

    companion object{
        @Volatile
        private var instance: LifeLabDatabase ?= null
        fun  getInstance(context: Context): LifeLabDatabase{
            return instance?:synchronized(this){
                instance ?: Room.databaseBuilder(context.applicationContext,
                    LifeLabDatabase::class.java, "lifelab.db").build()
                    .also { database -> instance = database }
            }
        }
    }
}
































