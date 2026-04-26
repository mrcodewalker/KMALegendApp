package com.example.kmalegend.network

import com.example.kmalegend.data.LoginResponse
import com.example.kmalegend.data.ScoreBatchResponse
import com.example.kmalegend.data.ScoresResponse
import com.example.kmalegend.data.ScholarshipStudent
import com.example.kmalegend.data.VirtualCalendarResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("encryption/public-key")
    suspend fun getPublicKey(): Response<String>

    @POST("auth/login")
    @Headers("X-Encrypted: true", "Content-Type: application/json")
    suspend fun login(@Body payload: RequestBody): Response<LoginResponse>

    @POST("auth/virtual-calendar")
    @Headers("X-Encrypted: true", "Content-Type: application/json")
    suspend fun loginVirtualCalendar(@Body payload: RequestBody): Response<VirtualCalendarResponse>

    @GET("scores/users/{studentCode}")
    suspend fun getScores(@Path("studentCode") studentCode: String): Response<ScoresResponse>

    @POST("score-batch/create-or-update")
    @Headers("X-Encrypted: true", "Content-Type: application/json")
    suspend fun saveScoreBatch(@Body payload: RequestBody): Response<Any>

    @POST("score-batch/get-by-encrypted")
    @Headers("X-Encrypted: true", "Content-Type: application/json")
    suspend fun getScoreBatch(@Body payload: RequestBody): Response<ScoreBatchResponse>

    @POST("scores/restore")
    @Headers("X-Encrypted: true", "Content-Type: application/json")
    suspend fun restoreScores(@Body payload: RequestBody): Response<ScoresResponse>

    @POST("semester/filter/scholarship")
    @Headers("X-Encrypted: true", "Content-Type: application/json")
    suspend fun getScholarship(@Body payload: RequestBody): Response<List<ScholarshipStudent>>
}
