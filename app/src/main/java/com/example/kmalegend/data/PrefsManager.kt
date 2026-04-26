package com.example.kmalegend.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PrefsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kma_legend_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val KEY_SCHEDULE_SECRET = "schedule_secret"
        const val KEY_VIRTUAL_CALENDAR_SECRET = "virtual_calendar_secret"
        const val KEY_VIRTUAL_SCORES_TABLE = "virtualScoresTable"
        const val KEY_VIRTUAL_SCORES_SNAPSHOT = "virtualScoresSnapshot"
        const val KEY_SCHEDULE_COURSE_COLORS = "schedule_course_colors"
        const val KEY_SAVED_CLASSES = "saved_classes"
        const val KEY_SCORES_SEARCH_HISTORY = "scores_search_history"
        const val KEY_RSA_PUBLIC_KEY = "rsa_public_key"
    }

    fun isLoggedIn(): Boolean = prefs.getString(KEY_SCHEDULE_SECRET, null) != null

    fun saveLoginData(scheduleJson: String, virtualCalJson: String) {
        prefs.edit()
            .putString(KEY_SCHEDULE_SECRET, scheduleJson)
            .apply()
        if (virtualCalJson.isNotEmpty()) {
            prefs.edit().putString(KEY_VIRTUAL_CALENDAR_SECRET, virtualCalJson).apply()
        }
    }

    fun getScheduleSecret(): LoginResponse? {
        val json = prefs.getString(KEY_SCHEDULE_SECRET, null) ?: return null
        return try { gson.fromJson(json, LoginResponse::class.java) } catch (e: Exception) { null }
    }

    fun getVirtualCalendarSecret(): VirtualCalendarResponse? {
        val json = prefs.getString(KEY_VIRTUAL_CALENDAR_SECRET, null) ?: return null
        return try { gson.fromJson(json, VirtualCalendarResponse::class.java) } catch (e: Exception) { null }
    }

    fun logout() {
        prefs.edit()
            .remove(KEY_SCHEDULE_SECRET)
            .remove(KEY_VIRTUAL_CALENDAR_SECRET)
            .remove(KEY_SAVED_CLASSES)
            .remove(KEY_VIRTUAL_SCORES_TABLE)
            .remove(KEY_VIRTUAL_SCORES_SNAPSHOT)
            .remove("credentials_username")
            .remove("credentials_password")
            .apply()
    }

    fun saveVirtualScores(scores: List<VirtualScore>) {
        prefs.edit().putString(KEY_VIRTUAL_SCORES_TABLE, gson.toJson(scores)).apply()
    }

    fun getVirtualScores(): List<VirtualScore> {
        val json = prefs.getString(KEY_VIRTUAL_SCORES_TABLE, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<VirtualScore>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { emptyList() }
    }

    fun saveVirtualScoresSnapshot(scores: List<VirtualScore>) {
        prefs.edit().putString(KEY_VIRTUAL_SCORES_SNAPSHOT, gson.toJson(scores)).apply()
    }

    fun getVirtualScoresSnapshot(): List<VirtualScore> {
        val json = prefs.getString(KEY_VIRTUAL_SCORES_SNAPSHOT, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<VirtualScore>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { emptyList() }
    }

    fun saveSavedClasses(classes: List<VirtualCalendarItem>) {
        prefs.edit().putString(KEY_SAVED_CLASSES, gson.toJson(classes)).apply()
    }

    fun getSavedClasses(): List<VirtualCalendarItem> {
        val json = prefs.getString(KEY_SAVED_CLASSES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<VirtualCalendarItem>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { emptyList() }
    }

    fun getSearchHistory(): List<String> {
        val json = prefs.getString(KEY_SCORES_SEARCH_HISTORY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { emptyList() }
    }

    fun addSearchHistory(code: String) {
        val history = getSearchHistory().toMutableList()
        history.remove(code)
        history.add(0, code)
        if (history.size > 10) history.removeAt(history.size - 1)
        prefs.edit().putString(KEY_SCORES_SEARCH_HISTORY, gson.toJson(history)).apply()
    }

    fun getRsaPublicKey(): String? = prefs.getString(KEY_RSA_PUBLIC_KEY, null)

    fun saveRsaPublicKey(key: String) {
        prefs.edit().putString(KEY_RSA_PUBLIC_KEY, key).apply()
    }

    fun saveCredentials(username: String, password: String) {
        prefs.edit()
            .putString("credentials_username", username)
            .putString("credentials_password", password)
            .apply()
    }

    fun getCredentials(): Pair<String, String>? {
        val u = prefs.getString("credentials_username", null) ?: return null
        val p = prefs.getString("credentials_password", null) ?: return null
        return Pair(u, p)
    }

    fun saveVirtualCalendarData(json: String) {
        prefs.edit().putString(KEY_VIRTUAL_CALENDAR_SECRET, json).apply()
    }

    fun getCourseColors(): Map<String, Int> {        val json = prefs.getString(KEY_SCHEDULE_COURSE_COLORS, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { emptyMap() }
    }

    fun saveCourseColors(colors: Map<String, Int>) {
        prefs.edit().putString(KEY_SCHEDULE_COURSE_COLORS, gson.toJson(colors)).apply()
    }
}
