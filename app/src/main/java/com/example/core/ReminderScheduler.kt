package com.example.core

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(ReminderScheduler.EXTRA_TYPE) ?: ReminderScheduler.TYPE_WORKOUT
        val title = when (type) {
            ReminderScheduler.TYPE_WATER -> "Hydratation · CAUSA SUI"
            ReminderScheduler.TYPE_BACKUP -> "Sauvegarde · CAUSA SUI"
            else -> "Séance du jour · CAUSA SUI"
        }
        val message = when (type) {
            ReminderScheduler.TYPE_WATER -> "Bois de l'eau. Hydrate-toi."
            ReminderScheduler.TYPE_BACKUP -> "Pense à exporter ta sauvegarde JSON locale."
            else -> "Aujourd'hui, tu bats la version d'hier. Lance ta séance."
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ReminderScheduler.ensureChannels(context)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context,
            type.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(type.hashCode(), notif)
        }
    }
}

object ReminderScheduler {
    const val CHANNEL_ID = "causa_sui_reminders"
    const val EXTRA_TYPE = "reminder_type"
    const val TYPE_WORKOUT = "workout"
    const val TYPE_WATER = "water"
    const val TYPE_BACKUP = "backup"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rappels CAUSA SUI",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Rappels d'entraînement, d'hydratation et de sauvegarde locale"
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun canUseExactAlarms(context: Context): Boolean {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            am.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleReminders(
        context: Context,
        workoutEnabled: Boolean,
        workoutHour: Int,
        workoutMinute: Int,
        waterEnabled: Boolean,
        waterStartHour: Int,
        waterIntervalHours: Int,
        backupMonthlyEnabled: Boolean,
    ) {
        ensureChannels(context)
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Workout daily alarm
        val workoutPi = pendingFor(context, TYPE_WORKOUT, 1001)
        am.cancel(workoutPi)
        if (workoutEnabled) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, workoutHour.coerceIn(0, 23))
                set(Calendar.MINUTE, workoutMinute.coerceIn(0, 59))
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            scheduleRepeatingCompat(am, cal.timeInMillis, AlarmManager.INTERVAL_DAY, workoutPi)
        }

        // Hydration repeating reminder
        val waterPi = pendingFor(context, TYPE_WATER, 1002)
        am.cancel(waterPi)
        if (waterEnabled) {
            val intervalMs = (waterIntervalHours.coerceIn(1, 6) * 60 * 60 * 1000L)
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, waterStartHour.coerceIn(5, 22))
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                while (timeInMillis <= System.currentTimeMillis()) {
                    timeInMillis += intervalMs
                }
            }
            scheduleRepeatingCompat(am, cal.timeInMillis, intervalMs, waterPi)
        }

        // Monthly backup reminder
        val backupPi = pendingFor(context, TYPE_BACKUP, 1003)
        am.cancel(backupPi)
        if (backupMonthlyEnabled) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 30)
                set(Calendar.HOUR_OF_DAY, 19)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            scheduleRepeatingCompat(am, cal.timeInMillis, AlarmManager.INTERVAL_DAY * 30L, backupPi)
        }
    }

    private fun scheduleRepeatingCompat(
        am: AlarmManager,
        triggerAtMs: Long,
        intervalMs: Long,
        pi: PendingIntent,
    ) {
        runCatching {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMs, intervalMs, pi)
        }
    }

    private fun pendingFor(context: Context, type: String, reqCode: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, type)
        }
        return PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
