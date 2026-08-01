package com.lifelab.server.core.config

data class AppConfig(
    val databaseUrl: String,
    val jwtSecret: String,
    val jwtIssuer: String = "lifelab-server",
    val jwtAudience: String = "lifelab-android",
    val jwtRealm: String = "LifeLab API",
) {
    companion object {
        fun fromEnvironment(): AppConfig = AppConfig(
            databaseUrl = System.getenv("LIFELAB_DATABASE_URL")
                ?: "jdbc:h2:file:./server-data/lifelab;DB_CLOSE_ON_EXIT=FALSE",
            jwtSecret = System.getenv("LIFELAB_JWT_SECRET")
                ?: "lifelab-local-development-secret-change-before-deployment",
        )
    }
}
