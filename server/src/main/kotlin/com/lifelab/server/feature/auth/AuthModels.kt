package com.lifelab.server.feature.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val account: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val account: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val user: UserResponse,
)

@Serializable
data class UserResponse(
    val id: String,
    val account: String,
)

data class StoredUser(
    val id: String,
    val account: String,
    val passwordHash: String,
    val passwordSalt: String,
) {
    fun toResponse() = UserResponse(id, account)
}
