package com.example.kmalegend.data

import com.example.kmalegend.network.EncryptionService
import com.example.kmalegend.network.RetrofitClient

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class Repository(private val prefs: PrefsManager) {

    private suspend fun getPublicKey(): String {
        // Cache lại, chỉ fetch 1 lần
        val cached = prefs.getRsaPublicKey()
        if (cached != null) return cached
        val response = RetrofitClient.api.getPublicKey()
        val key = response.body() ?: throw Exception("Không lấy được public key")
        prefs.saveRsaPublicKey(key)
        return key
    }

    suspend fun login(username: String, password: String): Result<Unit> {
        return try {
            val publicKey = getPublicKey()
            val credentials = mapOf("username" to username, "password" to password)
            val payload = EncryptionService.encryptPayload(credentials, publicKey)
            val response = RetrofitClient.api.login(EncryptionService.toRequestBody(payload))

            if (!response.isSuccessful) {
                val code = response.code()
                return Result.Error(
                    if (code == 401) "Sai mật khẩu hoặc mã sinh viên" else "Lỗi đăng nhập ($code)"
                )
            }
            val body = response.body()
            if (body?.code != "200") return Result.Error("Đăng nhập thất bại")

            // Lưu credentials để dùng lazy load sau
            val gson = com.google.gson.GsonBuilder().disableHtmlEscaping().create()
            prefs.saveLoginData(
                gson.toJson(body),
                "" // virtual calendar sẽ được load lazy khi cần
            )
            prefs.saveCredentials(username, password)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi không xác định")
        }
    }

    suspend fun loadVirtualCalendar(): Result<Unit> {
        return try {
            // Nếu đã có data thì không cần load lại
            val existing = prefs.getVirtualCalendarSecret()
            if (existing?.data != null) return Result.Success(Unit)

            val (username, password) = prefs.getCredentials()
                ?: return Result.Error("Chưa đăng nhập")

            val publicKey = getPublicKey()
            val credentials = mapOf("username" to username, "password" to password)
            val payload = EncryptionService.encryptPayload(credentials, publicKey)
            val response = RetrofitClient.api.loginVirtualCalendar(EncryptionService.toRequestBody(payload))

            if (!response.isSuccessful) {
                return Result.Error("Lỗi tải lịch ảo (${response.code()})")
            }
            val body = response.body()
            if (body?.code != "200") return Result.Error("Tải lịch ảo thất bại")

            val gson = com.google.gson.GsonBuilder().disableHtmlEscaping().create()
            prefs.saveVirtualCalendarData(gson.toJson(body))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getScores(studentCode: String): Result<ScoresResponse> {
        return try {
            val response = RetrofitClient.api.getScores(studentCode)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("Không có dữ liệu")
            } else {
                Result.Error("Lỗi tra cứu điểm (${response.code()})")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getScholarship(code: String): Result<List<ScholarshipStudent>> {
        return try {
            val publicKey = getPublicKey()
            val payload = EncryptionService.encryptPayload(mapOf("code" to code), publicKey)
            val response = RetrofitClient.api.getScholarship(EncryptionService.toRequestBody(payload))
            if (response.isSuccessful) {
                Result.Success(response.body() ?: emptyList())
            } else {
                Result.Error("Lỗi tải học bổng (${response.code()})")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getScoreBatch(studentCode: String): Result<ScoreBatchResponse> {
        return try {
            val publicKey = getPublicKey()
            val payload = EncryptionService.encryptPayload(mapOf("studentCode" to studentCode), publicKey)
            val response = RetrofitClient.api.getScoreBatch(EncryptionService.toRequestBody(payload))
            if (response.isSuccessful) {
                Result.Success(response.body() ?: ScoreBatchResponse())
            } else {
                Result.Error("Lỗi tải bảng điểm ảo (${response.code()})")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun restoreScores(studentCode: String): Result<ScoresResponse> {
        return try {
            val publicKey = getPublicKey()
            val payload = EncryptionService.encryptPayload(mapOf("studentCode" to studentCode), publicKey)
            val response = RetrofitClient.api.restoreScores(EncryptionService.toRequestBody(payload))
            if (response.isSuccessful) Result.Success(response.body() ?: ScoresResponse())
            else Result.Error("Lỗi khôi phục điểm (${response.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun saveScoreBatch(request: ScoreBatchRequest): Result<Unit> {
        return try {
            val publicKey = getPublicKey()
            // Dùng encryptObject để serialize đúng toàn bộ ScoreBatchRequest
            val body = EncryptionService.encryptObject(request, publicKey)
            val response = RetrofitClient.api.saveScoreBatch(body)
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error("Lỗi lưu bảng điểm (${response.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }
}
