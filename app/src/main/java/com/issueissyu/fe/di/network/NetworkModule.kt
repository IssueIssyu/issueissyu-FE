package com.issueissyu.fe.di.network

import android.util.Log
import com.google.gson.Gson
import com.issueissyu.fe.BuildConfig
import com.issueissyu.fe.core.constants.NetworkConstants
import com.issueissyu.fe.core.network.AuthInterceptor
import com.issueissyu.fe.core.network.TokenAuthenticator
import com.issueissyu.fe.data.remote.api.AiIssueApiService
import com.issueissyu.fe.data.remote.api.CommunityApi
import com.issueissyu.fe.data.remote.api.IssueApiService
import com.issueissyu.fe.data.remote.api.LocationApi
import com.issueissyu.fe.data.remote.api.MapApi
import com.issueissyu.fe.data.remote.api.MyPageApi
import com.issueissyu.fe.data.remote.api.PinApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val logger = HttpLoggingInterceptor.Logger { message ->
            Log.d("ISSUE_HTTP", message)
        }
        return HttpLoggingInterceptor(logger).apply {
            redactHeader("Authorization")
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(loggingInterceptor)
                }
            }
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("ai")
    fun provideAiRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NetworkConstants.AI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideIssueApiService(retrofit: Retrofit): IssueApiService {
        return retrofit.create(IssueApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAiIssueApiService(@Named("ai") retrofit: Retrofit): AiIssueApiService {
        return retrofit.create(AiIssueApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLocationApi(retrofit: Retrofit): LocationApi {
        return retrofit.create(LocationApi::class.java)
    }

    @Provides
    @Singleton
    fun providePinApi(retrofit: Retrofit): PinApi {
        return retrofit.create(PinApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMapApi(retrofit: Retrofit): MapApi {
        return retrofit.create(MapApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCommunityApi(retrofit: Retrofit): CommunityApi {
        return retrofit.create(CommunityApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMyPageApi(retrofit: Retrofit): MyPageApi {
        return retrofit.create(MyPageApi::class.java)
    }
}
