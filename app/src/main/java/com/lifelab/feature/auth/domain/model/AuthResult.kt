package com.lifelab.feature.auth.domain.model

sealed interface AuthResult {
    data class Success(val authResult: AuthUser) : AuthResult
    data class Failure(val message: String) : AuthResult
}

data class AuthUser(val id: String, val account: String)