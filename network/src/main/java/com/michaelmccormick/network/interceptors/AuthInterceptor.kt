package com.michaelmccormick.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException

internal class AuthInterceptor(
    private val accessToken: String,
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build(),
        )
    }
}
