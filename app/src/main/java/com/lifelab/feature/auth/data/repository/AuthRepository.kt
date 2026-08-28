package com.lifelab.feature.auth.data.repository

import com.google.gson.Gson
import com.lifelab.core.session.AuthTokenStore
import com.lifelab.feature.auth.data.remote.AuthApi
import com.lifelab.feature.auth.data.remote.AuthResponseDto
import com.lifelab.feature.auth.data.remote.LoginRequestDto
import com.lifelab.feature.auth.data.remote.RegisterRequestDto
import com.lifelab.feature.auth.domain.model.AuthResult
import com.lifelab.feature.auth.domain.model.AuthUser
import kotlinx.coroutines.CancellationException
import okio.IOException
import retrofit2.Response

class AuthRepository(
    private val authApi: AuthApi,
    private val authTokenStore: AuthTokenStore,
) {
    suspend fun login(
        account: String,
        password: String
    ): AuthResult {
        return try {
            val response = authApi.login(
                LoginRequestDto
                    (
                    account.trim(),
                    password
                )
            )
            if (response.isSuccessful) {
                handleLoginSuccess(response)
            } else {
                AuthResult.Failure("登录失败，请检查账号和密码")
            }

        } catch (error: CancellationException) {
            throw error
        } catch (error: IOException) {
            AuthResult.Failure(
                message = "无法连接服务器，请检查网络或确认后端已经启动"
            )
        } catch (error: Exception) {
            AuthResult.Failure(
                message = error.message ?: "登录失败，请稍后重试",
            )
        }
    }

    suspend fun register(
        account: String,
        password: String,
    ): AuthResult {
        return try {
            val response = authApi.register(
                RegisterRequestDto(
                    account = account.trim(),
                    password = password,
                )
            )

            if (response.isSuccessful) {
                handleLoginSuccess(response)
            } else {
                val message = when (response.code()) {
                    409 -> "该账号已经注册，请直接登录"
                    400 -> "账号或密码格式不正确"
                    else -> "注册失败，请稍后重试"
                }
                AuthResult.Failure(message)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: IOException) {
            AuthResult.Failure(
                "无法连接服务器，请检查网络或确认后端已经启动"
            )
        } catch (error: Exception) {
            AuthResult.Failure(
                error.message ?: "注册失败，请稍后重试"
            )
        }
    }


    private fun handleLoginSuccess(response: Response<AuthResponseDto>): AuthResult {
        val responseBody = response.body() ?: return AuthResult.Failure(
            message = "服务器返回数据为空"
        )

        authTokenStore.saveSession(
            token = responseBody.accessToken,
            userId = responseBody.user.id,
            account = responseBody.user.account,
        )
        return AuthResult.Success(
            AuthUser(
                responseBody.user.id,
                responseBody.user.account
            )
        )
    }



}
