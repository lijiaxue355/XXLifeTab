package com.lifelab.core.network

import com.lifelab.core.session.AuthTokenStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenStore: AuthTokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val assToken = tokenStore.getAccessToken()
        if(assToken.isNullOrBlank()){
            return chain.proceed(originalRequest)
        }
        val authorizedRequest = originalRequest
            .newBuilder()
            .header("Authorization","Bearer $assToken")
            .build()
        return chain.proceed(authorizedRequest)
    }

}