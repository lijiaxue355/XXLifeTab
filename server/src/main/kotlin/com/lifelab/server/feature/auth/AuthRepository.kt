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
        val account = request.account.trim()
        validateAccount(account)
        validatePassword(request.password)

        return DatabaseFactory.dbQuery {
            if (findByAccount(account) != null) {
                throw ApiException(HttpStatusCode.Conflict, "ACCOUNT_ALREADY_EXISTS", "该账号已经注册")
            }

            val userId = UUID.randomUUID().toString()
            val passwordHash = PasswordHasher.hash(request.password)
            val createdAt = System.currentTimeMillis()
            UsersTable.insert {
                it[id] = userId
                it[UsersTable.account] = account
                it[UsersTable.passwordHash] = passwordHash.hash
                it[passwordSalt] = passwordHash.salt
                it[createdAtMillis] = createdAt
            }
            StoredUser(
                id = userId,
                account = account,
                passwordHash = passwordHash.hash,
                passwordSalt = passwordHash.salt,
            )
        }
    }

    suspend fun authenticate(request: LoginRequest): StoredUser {
        val account = request.account.trim()
        validateAccount(account)
        val user = DatabaseFactory.dbQuery { findByAccount(account) }
            ?: throw invalidCredentials()

        if (!PasswordHasher.verify(request.password, user.passwordHash, user.passwordSalt)) {
            throw invalidCredentials()
        }
        return user
    }

    private fun findByAccount(account: String): StoredUser? = UsersTable
        .selectAll()
        .where { UsersTable.account eq account }
        .singleOrNull()
        ?.toStoredUser()

    private fun ResultRow.toStoredUser() = StoredUser(
        id = this[UsersTable.id],
        account = this[UsersTable.account],
        passwordHash = this[UsersTable.passwordHash],
        passwordSalt = this[UsersTable.passwordSalt],
    )

    private fun validateAccount(account: String) {
        if (!ACCOUNT_REGEX.matches(account)) {
            throw ApiException(HttpStatusCode.BadRequest, "INVALID_ACCOUNT", "账号应为 4 到 24 位字母、数字或下划线")
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
        "账号或密码错误",
    )

    companion object {
        private val ACCOUNT_REGEX = Regex("^[A-Za-z0-9_]{4,24}$")
    }
}
