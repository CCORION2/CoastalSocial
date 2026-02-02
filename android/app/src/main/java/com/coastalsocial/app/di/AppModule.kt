package com.coastalsocial.app.di

import android.content.Context
import com.coastalsocial.app.data.api.ApiClient
import com.coastalsocial.app.data.api.ApiService
import com.coastalsocial.app.data.local.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideApiClient(
        @ApplicationContext context: Context,
        tokenManager: TokenManager
    ): ApiClient {
        return ApiClient(context, tokenManager)
    }

    @Provides
    @Singleton
    fun provideApiService(apiClient: ApiClient): ApiService {
        return apiClient.apiService
    }
}
