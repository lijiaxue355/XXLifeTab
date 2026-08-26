package com.lifelab.feature.auth.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto,
    ): Response<AuthResponseDto>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto,
    ): Response<AuthResponseDto>
}
