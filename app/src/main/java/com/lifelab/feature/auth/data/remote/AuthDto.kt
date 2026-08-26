package com.lifelab.feature.auth.data.remote

data class RegisterRequestDto(
    val account: String,
    val password: String,
)

data class LoginRequestDto(
    val account: String,
    val password: String,
)

data class AuthResponseDto(
    val accessToken: String,
    val user: UserResponseDto,
)

data class UserResponseDto(
    val id: String,
    val account: String,
)

data class ErrorResponseDto(
    val code: String,
    val message: String,
)
