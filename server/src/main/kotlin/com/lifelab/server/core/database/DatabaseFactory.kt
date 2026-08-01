package com.lifelab.server.core.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {
    private lateinit var database: Database

    fun initialize(databaseUrl: String) {
        database = Database.connect(
            url = databaseUrl,
            driver = "org.h2.Driver",
            user = "sa",
            password = "",
        )

        transaction(database) {
            SchemaUtils.create(
                UsersTable,
                TemplatesTable,
                TemplateMetricsTable,
                ExperimentsTable,
                MetricsTable,
                DailyRecordsTable,
                RecordValuesTable,
            )
            seedTemplates()
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T = withContext(Dispatchers.IO) {
        transaction(database) {
            block()
        }
    }
}
