package com.lifelab.core.sync.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.POST

interface SyncApi {

    @GET("experiments")
    suspend fun getExperiments(): Response<List<RemoteExperimentDto>>

    @GET("records")
    suspend fun getRecords(): Response<List<RemoteRecordDto>>

    @DELETE("experiments/{experimentId}")
    suspend fun deleteExperiment(
        @Path("experimentId") experimentId: String,
    ): Response<Unit>

    @POST("experiments")
    suspend fun uploadExperiment(
        @Body request: UploadExperimentRequestDto,
    ): Response<SyncResponseDto>

    @POST("records")
    suspend fun uploadRecord(
        @Body request: UploadRecordRequestDto,
    ): Response<SyncResponseDto>
}
