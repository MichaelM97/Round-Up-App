package com.michaelmccormick.network.interceptors

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlin.test.assertEquals
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class AuthInterceptorTest {
    @Test
    fun `Should add Authorization header to request`() {
        // Given
        val accessToken = "123-abc_xyz?!"
        val authInterceptor = AuthInterceptor(accessToken = accessToken)
        val mockChain: Interceptor.Chain = mockk()
        val mockRequestBuilder: Request.Builder = mockk()
        every { mockRequestBuilder.addHeader(any(), any()) } returns mockRequestBuilder
        every { mockChain.request().newBuilder() } returns mockRequestBuilder
        val mockRequest: Request = mockk()
        every { mockRequestBuilder.build() } returns mockRequest
        val mockResponse: Response = mockk()
        every { mockChain.proceed(any()) } returns mockResponse

        // When
        val response = authInterceptor.intercept(mockChain)

        // Then
        verifyOrder {
            mockRequestBuilder.addHeader("Authorization", "Bearer $accessToken")
            mockChain.proceed(mockRequest)
        }
        assertEquals(mockResponse, response)
    }
}
