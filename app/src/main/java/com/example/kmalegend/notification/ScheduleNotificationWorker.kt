package com.example.kmalegend.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.kmalegend.data.CourseSchedule
import com.example.kmalegend.data.PrefsManager
import com.example.kmalegend.data.getLessonTime
import java.text.SimpleDateFormat
import java.util.*

class ScheduleNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val CHANNEL_ID         = "kma_schedule_channel"
        const val CHANNEL_NAME       = "Lịch học KMA"
        const val KEY_MINUTES_BEFORE = "minutes_before"
        const val KEY_TARGET_DATE    = "target_date"

        /**
         * Find the next upcoming lesson date and fire a test notification immediately.
         * - If today still has lessons that haven't ended (last lesson ends 20:30), use today.
         * - Otherwise find the next calendar date that has lessons.
         */
        fun sendTestNotification(context: Context) {
            val prefs    = PrefsManager(context)
            val schedule = prefs.getScheduleSecret()?.data?.student_schedule ?: return
            val sdf      = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val now      = Calendar.getInstance()
            val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val LAST_LESSON_END = 20 * 60 + 30  // 20:30

            val allDateStrs = schedule
                .flatMap { c -> c.study_days.split(" ").map { it.trim() } }
                .filter { it.isNotEmpty() }
                .toSet()

            val todayStr = sdf.format(now.time)

            val targetDate: String? = when {
                todayStr in allDateStrs && nowMinutes < LAST_LESSON_END -> todayStr
                else -> {
                    val todayMidnight = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                    }.time
                    allDateStrs
                        .mapNotNull { dateStr ->
                            try { sdf.parse(dateStr)?.let { dateStr to it } } catch (e: Exception) { null }
                        }
                        .sortedBy { it.second }
                        .firstOrNull { (_, date) -> date.after(todayMidnight) }
                        ?.first
                }
            }

            if (targetDate == null) return
            val courses = schedule.filter { c ->
                c.study_days.split(" ").any { it.trim() == targetDate }
            }
            if (courses.isEmpty()) return

            fireNotif(context, courses, targetDate)
        }

        fun fireNotif(context: Context, courses: List<CourseSchedule>, targetDate: String) {
            ensureChannel(context)

            val tapIntent = Intent(context, com.example.kmalegend.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 999, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val largeBitmap = try {
                BitmapFactory.decodeResource(context.resources, com.example.kmalegend.R.drawable.logo)
            } catch (e: Exception) { null }

            val courseLines = courses.map { c ->
                val time = getLessonTime(c.lessons.split(" ").firstOrNull() ?: "")
                "• ${c.course_name}  ${time.start}–${time.end}  📍${c.study_location}"
            }
            val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle("🔔 Test — Lịch học $targetDate")
                .setSummaryText("${courses.size} môn học")
            courseLines.forEach { inboxStyle.addLine(it) }

            val notif = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(com.example.kmalegend.R.drawable.ic_launcher_foreground)
                .setLargeIcon(largeBitmap)
                .setContentTitle("🔔 Test — Lịch học $targetDate")
                .setContentText("${courses.size} môn: ${courses.joinToString(", ") { it.course_name }}")
                .setStyle(inboxStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(0xFFCC0000.toInt())
                .setVibrate(longArrayOf(0, 250, 100, 250))
                .build()

            try {
                NotificationManagerCompat.from(context).notify(9999, notif)
            } catch (e: SecurityException) { /* permission not granted */ }
        }

        private fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Nhắc nhở lịch học sắp tới"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 100, 250)
                }
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(channel)
            }
        }
    }

    override fun doWork(): Result {
        val minutesBefore = inputData.getInt(KEY_MINUTES_BEFORE, 60)
        val targetDate    = inputData.getString(KEY_TARGET_DATE) ?: run {
            val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, minutesBefore) }
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
        }

        val prefs    = PrefsManager(context)
        val schedule = prefs.getScheduleSecret()?.data?.student_schedule ?: return Result.success()

        val courses = schedule.filter { course ->
            course.study_days.split(" ").any { it.trim() == targetDate }
        }
        if (courses.isEmpty()) return Result.success()

        ensureChannel(context)

        val tapIntent = Intent(context, com.example.kmalegend.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, minutesBefore, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val largeBitmap = try {
            BitmapFactory.decodeResource(context.resources, com.example.kmalegend.R.drawable.logo)
        } catch (e: Exception) { null }

        val label = NotificationScheduler.OFFSETS
            .firstOrNull { it.first == minutesBefore }?.second ?: "$minutesBefore phút nữa"

        val emoji = when {
            minutesBefore >= 1440 -> "📅"
            minutesBefore >= 720  -> "🌙"
            minutesBefore >= 60   -> "⏰"
            minutesBefore >= 30   -> "🔔"
            minutesBefore >= 15   -> "⚡"
            else                  -> "🚨"
        }

        val courseLines = courses.map { c ->
            val time = getLessonTime(c.lessons.split(" ").firstOrNull() ?: "")
            "• ${c.course_name}  ${time.start}–${time.end}  📍${c.study_location}"
        }
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("$emoji Lịch học $label — $targetDate")
            .setSummaryText("${courses.size} môn học")
        courseLines.forEach { inboxStyle.addLine(it) }

        val notifId = minutesBefore + targetDate.replace("/", "").hashCode()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.example.kmalegend.R.drawable.ic_launcher_foreground)
            .setLargeIcon(largeBitmap)
            .setContentTitle("$emoji Lịch học $label")
            .setContentText(
                if (courses.size == 1) courseLines.first()
                else "${courses.size} môn: ${courses.joinToString(", ") { it.course_name }}"
            )
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0xFFCC0000.toInt())
            .setColorized(true)
            .setVibrate(longArrayOf(0, 250, 100, 250))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (e: SecurityException) { /* permission revoked */ }

        return Result.success()
    }
}
