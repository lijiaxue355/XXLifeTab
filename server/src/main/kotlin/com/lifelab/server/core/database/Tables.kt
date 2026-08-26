package com.lifelab.server.core.database

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object UsersTable : Table("users") {
    val id = varchar("id", 36)
    val account = varchar("account", 24).uniqueIndex()
    val passwordHash = varchar("password_hash", 128)
    val passwordSalt = varchar("password_salt", 64)
    val createdAtMillis = long("created_at_millis")

    override val primaryKey = PrimaryKey(id)
}

object ExperimentsTable : Table("experiments") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36)
        .references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
        .index()
    val templateId = varchar("template_id", 36).nullable()
    val name = varchar("name", 120)
    val hypothesis = varchar("hypothesis", 500)
    val description = text("description")
    val coverColor = varchar("cover_color", 32)
    val startDateMillis = long("start_date_millis")
    val durationDays = integer("duration_days")
    val baselineDays = integer("baseline_days")
    val interventionDays = integer("intervention_days")
    val createdAtMillis = long("created_at_millis")
    val updatedAtMillis = long("updated_at_millis")

    override val primaryKey = PrimaryKey(id)
}

object MetricsTable : Table("metrics") {
    val id = varchar("id", 36)
    val experimentId = varchar("experiment_id", 36)
        .references(ExperimentsTable.id, onDelete = ReferenceOption.CASCADE)
        .index()
    val name = varchar("name", 120)
    val type = varchar("type", 32)
    val required = bool("required")
    val sortOrder = integer("sort_order")

    override val primaryKey = PrimaryKey(id)
}

object TemplatesTable : Table("experiment_templates") {
    val id = varchar("id", 36)
    val name = varchar("name", 120)
    val hypothesis = varchar("hypothesis", 500)
    val description = text("description")
    val coverImageUrl = varchar("cover_image_url", 1_000)
    val durationDays = integer("duration_days")
    val baselineDays = integer("baseline_days")
    val interventionDays = integer("intervention_days")

    override val primaryKey = PrimaryKey(id)
}

object TemplateMetricsTable : Table("template_metrics") {
    val id = varchar("id", 36)
    val templateId = varchar("template_id", 36)
        .references(TemplatesTable.id, onDelete = ReferenceOption.CASCADE)
        .index()
    val name = varchar("name", 120)
    val type = varchar("type", 32)
    val required = bool("required")
    val sortOrder = integer("sort_order")

    override val primaryKey = PrimaryKey(id)
}

object DailyRecordsTable : Table("daily_records") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36)
        .references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
        .index()
    val experimentId = varchar("experiment_id", 36)
        .references(ExperimentsTable.id, onDelete = ReferenceOption.CASCADE)
        .index()
    val recordedAtMillis = long("recorded_at_millis")
    val note = text("note").nullable()
    val createdAtMillis = long("created_at_millis")
    val updatedAtMillis = long("updated_at_millis")

    override val primaryKey = PrimaryKey(id)
}

object RecordValuesTable : Table("record_values") {
    val id = varchar("id", 36)
    val recordId = varchar("record_id", 36)
        .references(DailyRecordsTable.id, onDelete = ReferenceOption.CASCADE)
        .index()
    val metricId = varchar("metric_id", 36)
        .references(MetricsTable.id, onDelete = ReferenceOption.CASCADE)
        .index()
    val value = varchar("value", 500)

    override val primaryKey = PrimaryKey(id)
}
