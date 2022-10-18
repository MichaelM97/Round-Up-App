package com.michaelmccormick.network.di

import com.michaelmccormick.network.interceptors.AuthInterceptor
import com.michaelmccormick.network.services.StarlingAPIService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Singleton
    @Provides
    fun provideEmployeeService(): StarlingAPIService {
        val client = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(accessToken = provideAccessToken()))
            .build()
        return Retrofit.Builder()
            .baseUrl("https://api-sandbox.starlingbank.com/api/v2/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    // If this was a real application the user would complete an authentication flow, their resulting access token
    // would be stored securely (e.g. in encrypted shared preferences) and then retrieved by this provider.
    private fun provideAccessToken(): String {
        return ""
    }
}
