package com.lifelab.server

import com.lifelab.server.core.config.AppConfig
import com.lifelab.server.feature.auth.AuthResponse
import com.lifelab.server.feature.auth.LoginRequest
import com.lifelab.server.feature.auth.RegisterRequest
import com.lifelab.server.feature.experiment.CreateExperimentRequest
import com.lifelab.server.feature.experiment.CreateMetricRequest
import com.lifelab.server.feature.experiment.ExperimentResponse
import com.lifelab.server.feature.record.CreateRecordRequest
import com.lifelab.server.feature.record.CreateRecordValueRequest
import com.lifelab.server.feature.record.RecordResponse
import com.lifelab.server.feature.template.TemplateResponse
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun `authentication experiments and records work end to end`() = testApplication {
        application {
            module(
                AppConfig(
                    databaseUrl = "jdbc:h2:mem:${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
                    jwtSecret = "test-secret-with-at-least-thirty-two-characters",
                ),
            )
        }
        val apiClient = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        assertEquals(HttpStatusCode.OK, apiClient.get("/health").status)
        assertEquals(
            HttpStatusCode.Unauthorized,
            apiClient.get("/api/v1/experiments").status,
        )

        register(apiClient, "owner@lifelab.test", "实验者")
        val loginResponse = apiClient.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("owner@lifelab.test", "password123"))
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val ownerAuth = loginResponse.body<AuthResponse>()
        assertTrue(ownerAuth.accessToken.isNotBlank())
        val templatesResponse = apiClient.get("/api/v1/templates")
        assertEquals(HttpStatusCode.OK, templatesResponse.status)
        assertTrue(templatesResponse.body<List<TemplateResponse>>().isNotEmpty())

        val experimentId = "33333333-3333-4333-8333-333333333333"
        val completionMetricId = "33333333-3333-4333-8333-333333333301"
        val energyMetricId = "33333333-3333-4333-8333-333333333302"
        val experimentRequest = CreateExperimentRequest(
            id = experimentId,
            name = "测试睡前不看手机",
            hypothesis = "减少屏幕刺激可以改善睡眠",
            description = "端到端测试实验",
            startDateMillis = 1_800_000_000_000,
            durationDays = 14,
            baselineDays = 7,
            interventionDays = 7,
            metrics = listOf(
                CreateMetricRequest(completionMetricId, "是否完成干预", "YES_NO", true, 0),
                CreateMetricRequest(energyMetricId, "晨间精力", "SCORE", true, 1),
            ),
        )

        val createExperiment = apiClient.post("/api/v1/experiments") {
            bearerAuth(ownerAuth.accessToken)
            contentType(ContentType.Application.Json)
            setBody(experimentRequest)
        }
        assertEquals(HttpStatusCode.Created, createExperiment.status)
        assertEquals(experimentId, createExperiment.body<ExperimentResponse>().id)

        val retryExperiment = apiClient.post("/api/v1/experiments") {
            bearerAuth(ownerAuth.accessToken)
            contentType(ContentType.Application.Json)
            setBody(experimentRequest)
        }
        assertEquals(HttpStatusCode.Created, retryExperiment.status)

        val ownerExperiments = apiClient.get("/api/v1/experiments") {
            bearerAuth(ownerAuth.accessToken)
        }.body<List<ExperimentResponse>>()
        assertEquals(1, ownerExperiments.size)
        assertEquals(2, ownerExperiments.single().metrics.size)

        val recordId = "44444444-4444-4444-8444-444444444444"
        val recordRequest = CreateRecordRequest(
            id = recordId,
            experimentId = experimentId,
            recordedAtMillis = 1_800_000_100_000,
            note = "今天执行顺利",
            values = listOf(
                CreateRecordValueRequest(completionMetricId, "true"),
                CreateRecordValueRequest(energyMetricId, "8"),
            ),
        )
        val createRecord = apiClient.post("/api/v1/records") {
            bearerAuth(ownerAuth.accessToken)
            contentType(ContentType.Application.Json)
            setBody(recordRequest)
        }
        assertEquals(HttpStatusCode.Created, createRecord.status)
        assertEquals(recordId, createRecord.body<RecordResponse>().id)

        val retryRecord = apiClient.post("/api/v1/records") {
            bearerAuth(ownerAuth.accessToken)
            contentType(ContentType.Application.Json)
            setBody(recordRequest)
        }
        assertEquals(HttpStatusCode.Created, retryRecord.status)

        val records = apiClient.get("/api/v1/records?experimentId=$experimentId") {
            bearerAuth(ownerAuth.accessToken)
        }.body<List<RecordResponse>>()
        assertEquals(1, records.size)
        assertEquals(2, records.single().values.size)

        val secondUserAuth = register(apiClient, "second@lifelab.test", "另一个用户")
        val secondUserExperiments = apiClient.get("/api/v1/experiments") {
            bearerAuth(secondUserAuth.accessToken)
        }.body<List<ExperimentResponse>>()
        assertTrue(secondUserExperiments.isEmpty())
    }

    private suspend fun register(
        client: io.ktor.client.HttpClient,
        email: String,
        displayName: String,
    ): AuthResponse {
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, "password123", displayName))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        return response.body<AuthResponse>().also {
            assertNotNull(it.accessToken)
        }
    }
}
