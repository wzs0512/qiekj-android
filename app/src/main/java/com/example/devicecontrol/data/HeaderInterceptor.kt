package com.example.devicecontrol.data

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(
    private val tokenProvider: () -> String?,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val timestamp = System.currentTimeMillis().toString()
        val isLoginApi = chain.request().url.encodedPath.startsWith("/common/")
            || chain.request().url.encodedPath.startsWith("/user/reg")
        val builder = chain.request().newBuilder()
            .header("Version", ApiConfig.VERSION)
            .header("channel", if (isLoginApi) ApiConfig.LOGIN_CHANNEL else ApiConfig.API_CHANNEL)
            .header("phoneBrand", ApiConfig.PHONE_BRAND)
            .header("User-Agent", ApiConfig.USER_AGENT)
            .header("Content-Type", ApiConfig.CONTENT_TYPE)
            .header("timestamp", timestamp)
            .header("Host", "userapi.qiekj.com")
            .header("Connection", "Keep-Alive")
            .header("Accept-Encoding", "gzip")

        tokenProvider()?.takeIf { it.isNotBlank() }?.let { token ->
            builder.header("token", token)
            builder.header("Authorization", token)
        }

        return chain.proceed(builder.build())
    }
}
