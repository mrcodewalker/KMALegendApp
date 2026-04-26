package com.example.kmalegend.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://kma-legend.click/api/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val body = request.body
            if (body != null) {
                val buffer = okio.Buffer()
                body.writeTo(buffer)
                val bodyStr = buffer.readUtf8()
                val ivMatch = Regex("\"iv\":\"([^\"]+)\"").find(bodyStr)?.groupValues?.get(1)
                android.util.Log.e("OkHttpIV", "IV sent: '$ivMatch' length=${ivMatch?.length}")
            }
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .disableHtmlEscaping()
        .create()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())  // plain text trước
            .addConverterFactory(GsonConverterFactory.create(gson)) // JSON sau
            .build()
            .create(ApiService::class.java)
    }
}
