package com.lifelab.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lifelab.core.sync.data.local.dao.OutboxDao
import com.lifelab.core.sync.data.local.entity.OutboxEntity
import com.lifelab.feature.experiment.data.local.dao.ExperimentDao
import com.lifelab.feature.experiment.data.local.entity.ExperimentEntity
import com.lifelab.feature.experiment.data.local.entity.MetricEntity
import com.lifelab.feature.record.data.local.dao.RecordDao
import com.lifelab.feature.record.data.local.entity.DailyRecordEntity

@Database(
    entities = [
        ExperimentEntity::class,
        MetricEntity::class,
        DailyRecordEntity::class,
        OutboxEntity::class,
    ],
    version = 2,
    exportSchema = false,
)

abstract class LifeLabDatabase : RoomDatabase(){

    abstract fun experimentDao() : ExperimentDao
    abstract fun recordDao(): RecordDao

    abstract fun outboxDao(): OutboxDao

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
































