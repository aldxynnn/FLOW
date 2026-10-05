package com.aldxynnn.flow.core.network

import com.aldxynnn.flow.core.AppConfig
import com.aldxynnn.flow.core.session.SessionStore
import okhttp3.Dns
import okhttp3.Interceptor
import java.net.Proxy
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiProvider(sessionStore: SessionStore) {
    private val authInterceptor = Interceptor { chain ->
        val token = kotlinx.coroutines.runBlocking { sessionStore.token() }
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) addHeader("Authorization", "Bearer $token")
        }.build()
        chain.proceed(request)
    }

    private val httpClient = OkHttpClient.Builder()
        // The emulator can inherit a host proxy configuration. Backend traffic to the
        // local emulator bridge must go directly to the host, not through that proxy.
        .proxy(Proxy.NO_PROXY)
        .dns(Dns.SYSTEM)
        .retryOnConnectionFailure(true)
        .addInterceptor(authInterceptor)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (com.aldxynnn.flow.BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
        )
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    val service: ApiService = Retrofit.Builder()
        .baseUrl(AppConfig.API_BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}
