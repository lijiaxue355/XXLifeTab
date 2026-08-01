package com.lifelab.server.feature.auth

import com.lifelab.server.core.database.DatabaseFactory
import com.lifelab.server.core.database.UsersTable
import com.lifelab.server.core.error.ApiException
import com.lifelab.server.core.security.PasswordHasher
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.UUID

class AuthRepository {
    suspend fun register(request: RegisterRequest): StoredUser {
        val email = request.email.trim().lowercase()
        val displayName = request.displayName.trim()
        validateEmail(email)
        validatePassword(request.password)
        if (displayName.isBlank() || displayName.length > 80) {
            throw ApiException(HttpStatusCode.BadRequest, "INVALID_DISPLAY_NAME", "昵称长度应为 1 到 80 个字符")
        }

        return DatabaseFactory.dbQuery {
            if (findByEmail(email) != null) {
                throw ApiException(HttpStatusCode.Conflict, "EMAIL_ALREADY_EXISTS", "该邮箱已经注册")
            }

            val userId = UUID.randomUUID().toString()
            val passwordHash = PasswordHasher.hash(request.password)
            val createdAt = System.currentTimeMillis()
            UsersTable.insert {
                it[id] = userId
                it[UsersTable.email] = email
                it[UsersTable.displayName] = displayName
                it[UsersTable.passwordHash] = passwordHash.hash
                it[passwordSalt] = passwordHash.salt
                it[createdAtMillis] = createdAt
            }
            StoredUser(
                id = userId,
                email = email,
                displayName = displayName,
                passwordHash = passwordHash.hash,
                passwordSalt = passwordHash.salt,
            )
        }
    }

    suspend fun authenticate(request: LoginRequest): StoredUser {
        val email = request.email.trim().lowercase()
        validateEmail(email)
        val user = DatabaseFactory.dbQuery { findByEmail(email) }
            ?: throw invalidCredentials()

        if (!PasswordHasher.verify(request.password, user.passwordHash, user.passwordSalt)) {
            throw invalidCredentials()
        }
        return user
    }

    private fun findByEmail(email: String): StoredUser? = UsersTable
        .selectAll()
        .where { UsersTable.email eq email }
        .singleOrNull()
        ?.toStoredUser()

    private fun ResultRow.toStoredUser() = StoredUser(
        id = this[UsersTable.id],
        email = this[UsersTable.email],
        displayName = this[UsersTable.displayName],
        passwordHash = this[UsersTable.passwordHash],
        passwordSalt = this[UsersTable.passwordSalt],
    )

    private fun validateEmail(email: String) {
        if (email.length !in 3..320 || !EMAIL_REGEX.matches(email)) {
            throw ApiException(HttpStatusCode.BadRequest, "INVALID_EMAIL", "请输入有效邮箱")
        }
    }

    private fun validatePassword(password: String) {
        if (password.length !in 8..128) {
            throw ApiException(HttpStatusCode.BadRequest, "INVALID_PASSWORD", "密码长度应为 8 到 128 个字符")
        }
    }

    private fun invalidCredentials() = ApiException(
        HttpStatusCode.Unauthorized,
        "INVALID_CREDENTIALS",
        "邮箱或密码错误",
    )

    companion object {
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
