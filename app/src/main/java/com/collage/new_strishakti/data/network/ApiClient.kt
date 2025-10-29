package com.collage.new_strishakti.data.network

import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {


  // private const val BASE_URL = "https://backend.strishakti.org/"
  private const val BASE_URL = "https://dev.api.strishakti.org/"

    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }


    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            // ✅ Reuse TCP connections across calls
            .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
            // ✅ Avoid DNS re-resolution every request
            .dns(Dns.SYSTEM)
            // ✅ Reasonable timeouts
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            // ✅ Add interceptor for logging (optional)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}