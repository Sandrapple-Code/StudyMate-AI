package com.example

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.StudyMateDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimetableReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val database = StudyMateDatabase.getDatabase(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = database.dao()
                val activeItems = dao.getAllEnabledRemindersDirect()

                val sdfDay = SimpleDateFormat("EEEE", Locale.US)
                val sdfTime = SimpleDateFormat("HH:mm", Locale.US)
                val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

                val now = Date()
                val dayName = sdfDay.format(now)
                val timeStr = sdfTime.format(now)

                val prefs = context.getSharedPreferences("studymate_prefs", Context.MODE_PRIVATE)
                val lastCleanedDate = prefs.getString("last_cleaned_date", "")
                val savedStamps = prefs.getStringSet("triggered_stamps", null)?.toMutableSet() ?: mutableSetOf()

                val triggeredStamps = if (lastCleanedDate != todayDateStr) {
                    val freshMap = mutableSetOf<String>()
                    prefs.edit()
                        .putString("last_cleaned_date", todayDateStr)
                        .putStringSet("triggered_stamps", freshMap)
                        .apply()
                    freshMap
                } else {
                    savedStamps
                }

                for (item in activeItems) {
                    if (item.dayOfWeek.equals(dayName, ignoreCase = true) && item.time == timeStr) {
                        val stamp = "${item.id}-$todayDateStr"
                        if (!triggeredStamps.contains(stamp)) {
                            triggeredStamps.add(stamp)
                            prefs.edit().putStringSet("triggered_stamps", triggeredStamps).apply()

                            // Trigger sound and show system notification!
                            sendSystemNotification(context, item.subject, "Reminder: Scheduled class/study session started!")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TimetableReceiver", "Error checking reminders: ${e.message}", e)
            } finally {
                // Schedule the next check
                scheduleNextAlarm(context)
                pendingResult.finish()
            }
        }
    }

    private fun sendSystemNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "studymate_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "StudyMate Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Class and Study Timetable Reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Open app when user clicks notification
        val openIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = if (openIntent != null) {
            PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            null
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("StudyMate Class Schedule: $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        fun scheduleNextAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimetableReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val now = System.currentTimeMillis()
            // Align with the exact next minute boundary for precision check
            val nextMinute = now - (now % 60000) + 60000

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Schedule with precision
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            nextMinute,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            nextMinute,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextMinute,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextMinute,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextMinute,
                    pendingIntent
                )
            }
        }
    }
}
