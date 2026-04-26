package com.example.kmalegend.network

import android.util.Base64
import com.example.kmalegend.data.EncryptedPayload
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionService {
    private val gson = com.google.gson.GsonBuilder().disableHtmlEscaping().create()

    fun encryptPayload(data: Any, publicKeyPem: String): EncryptedPayload {
        // 1. Parse RSA public key — strip PEM headers + all whitespace
        val cleanPem = publicKeyPem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("-----BEGIN RSA PUBLIC KEY-----", "")
            .replace("-----END RSA PUBLIC KEY-----", "")
            .replace("\n", "")
            .replace("\r", "")
            .replace(" ", "")
            .trim()

        android.util.Log.d("EncryptionService", "PEM length after clean: ${cleanPem.length}")

        val keyBytes = Base64.decode(cleanPem, Base64.DEFAULT)
        android.util.Log.d("EncryptionService", "RSA key bytes length: ${keyBytes.size}")

        val rsaKey = KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(keyBytes))

        // 2. Generate AES-256 key (32 bytes) + IV (16 bytes)
        val secureRandom = SecureRandom()
        val aesKey = ByteArray(32).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(16).also { secureRandom.nextBytes(it) }

        // AES key dưới dạng hex string (server expect hex string sau RSA decrypt)
        val aesKeyHex = buildString {
            for (b in aesKey) append(String.format("%02x", b.toInt() and 0xFF))
        }

        // 3. Encrypt JSON with AES-256-CBC
        val aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        aesCipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(aesKey, "AES"),
            IvParameterSpec(iv)
        )
        val jsonStr = gson.toJson(data)
        android.util.Log.d("EncryptionService", "JSON to encrypt: $jsonStr")
        val encryptedDataBytes = aesCipher.doFinal(jsonStr.toByteArray(Charsets.UTF_8))
        val encryptedData = Base64.encodeToString(encryptedDataBytes, Base64.NO_WRAP)

        // 4. Encrypt AES key HEX STRING với RSA (server dùng hexStringToByteArray sau decrypt)
        val rsaCipher = try {
            Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL")
        } catch (e: Exception) {
            Cipher.getInstance("RSA/ECB/PKCS1Padding")
        }
        rsaCipher.init(Cipher.ENCRYPT_MODE, rsaKey)
        // Encrypt hex string bytes, không phải raw key bytes
        val encryptedKeyBytes = rsaCipher.doFinal(aesKeyHex.toByteArray(Charsets.UTF_8))
        val encryptedKey = Base64.encodeToString(encryptedKeyBytes, Base64.NO_WRAP)

        android.util.Log.d("EncryptionService", "encryptedKey length: ${encryptedKey.length}")
        android.util.Log.d("EncryptionService", "encryptedData length: ${encryptedData.length}")

        // 5. IV → hex, dùng Locale.US + mask unsigned, đảm bảo đúng 32 chars
        val ivHexChars = CharArray(32)
        val hexDigits = "0123456789abcdef"
        for (i in iv.indices) {
            val b = iv[i].toInt() and 0xFF
            ivHexChars[i * 2] = hexDigits[b ushr 4]
            ivHexChars[i * 2 + 1] = hexDigits[b and 0x0F]
        }
        val ivHex = String(ivHexChars)

        android.util.Log.d("EncryptionService", "ivHex length: ${ivHex.length}, value: $ivHex")
        android.util.Log.d("EncryptionService", "encryptedKey length: ${encryptedKey.length}")
        check(ivHex.length == 32) { "IV hex must be 32 chars, got ${ivHex.length}" }

        return EncryptedPayload(encryptedKey, encryptedData, ivHex)
    }

    // Tạo RequestBody trực tiếp — bypass Retrofit Gson converter hoàn toàn
    fun toRequestBody(payload: EncryptedPayload): okhttp3.RequestBody {
        val json = """{"encryptedKey":"${payload.encryptedKey}","encryptedData":"${payload.encryptedData}","iv":"${payload.iv}"}"""
        android.util.Log.d("EncryptionService", "Final JSON body: $json")
        android.util.Log.d("EncryptionService", "IV in body: '${payload.iv}' length=${payload.iv.length}")
        return json.toRequestBody("application/json; charset=utf-8".toMediaType())
    }

    // Serialize object thành JSON rồi encrypt — dùng cho các request phức tạp
    fun encryptObject(data: Any, publicKeyPem: String): okhttp3.RequestBody {
        return toRequestBody(encryptPayload(data, publicKeyPem))
    }
}
