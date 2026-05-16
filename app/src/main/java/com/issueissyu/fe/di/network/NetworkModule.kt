package com.issueissyu.fe.di.network

import android.util.Log
import com.google.gson.Gson
import com.issueissyu.fe.BuildConfig
import com.issueissyu.fe.core.constants.NetworkConstants
import com.issueissyu.fe.core.network.AuthInterceptor
import com.issueissyu.fe.core.network.TokenAuthenticator
import com.issueissyu.fe.data.remote.api.AuthApi
import com.issueissyu.fe.data.remote.api.IssueApiService
import com.issueissyu.fe.data.remote.api.LocationApi
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
            // Logcat에서 확실하게 잡히도록 별도 태그 사용
            Log.d("ISSUE_HTTP", message)
        }
        return HttpLoggingInterceptor(logger).apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
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
            .addInterceptor(loggingInterceptor)
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
    fun provideIssueApiService(retrofit: Retrofit): IssueApiService {
        return retrofit.create(IssueApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLocationApi(retrofit: Retrofit): LocationApi {
        return retrofit.create(LocationApi::class.java)
    }
}