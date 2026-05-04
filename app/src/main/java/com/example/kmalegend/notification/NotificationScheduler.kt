package com.example.kmalegend.notification

import android.content.Context
import androidx.work.*
import com.example.kmalegend.data.PrefsManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    // Each entry: minutes before lesson start → display label
    val OFFSETS: List<Pair<Int, String>> = listOf(
        720  to "12 giờ nữa",
        360  to "6 giờ nữa",
        60   to "1 giờ nữa",
        30   to "30 phút nữa",
        15   to "15 phút nữa",
        5    to "5 phút nữa"
    )

    fun schedule(context: Context) {
        val prefs    = PrefsManager(context)
        val schedule = prefs.getScheduleSecret()?.data?.student_schedule ?: return
        val sdf      = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val now      = System.currentTimeMillis()

        WorkManager.getInstance(context).cancelAllWorkByTag("kma_schedule_notification")

        val futureDates = schedule
            .flatMap { c -> c.study_days.split(" ").map { it.trim() } }
            .toSet()
            .mapNotNull { dateStr ->
                try {
                    val cal = Calendar.getInstance().apply {
                        time = sdf.parse(dateStr) ?: return@mapNotNull null
                        // Assume first lesson starts at 07:00
                        set(Calendar.HOUR_OF_DAY, 7)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (cal.timeInMillis > now) dateStr to cal.timeInMillis else null
                } catch (e: Exception) { null }
            }

        futureDates.forEach { (dateStr, lessonStartMs) ->
            OFFSETS.forEach { (minutes, _) ->
                val fireAt = lessonStartMs - TimeUnit.MINUTES.toMillis(minutes.toLong())
                val delay  = fireAt - now
                if (delay <= 0) return@forEach

                val data = workDataOf(
                    ScheduleNotificationWorker.KEY_MINUTES_BEFORE to minutes,
                    ScheduleNotificationWorker.KEY_TARGET_DATE    to dateStr
                )
                val tag = "kma_notif_${dateStr}_${minutes}m"
                val request = OneTimeWorkRequestBuilder<ScheduleNotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag("kma_schedule_notification")
                    .addTag(tag)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    tag, ExistingWorkPolicy.REPLACE, request
                )
            }
        }
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("kma_schedule_notification")
    }
}
