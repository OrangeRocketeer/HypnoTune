package com.samsung.health.mobile.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.samsung.health.mobile.R
import com.samsung.health.mobile.presentation.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ProfileReminderManager"

@Singleton
class ProfileReminderManager @Inject constructor() {

    private var sharedPrefs: SharedPreferences? = null

    companion object {
        private const val PREFS_NAME = "profile_reminder_prefs"
        private const val KEY_LAST_PROFILE_CHANGE = "last_profile_change_timestamp"
        private const val KEY_LAST_REMINDER_SHOWN = "last_reminder_shown_timestamp"
        private const val KEY_REMINDER_DISMISSED_COUNT = "reminder_dismissed_count"

        private const val REMINDER_INTERVAL_DAYS = 21
        private const val REMINDER_INTERVAL_MS = REMINDER_INTERVAL_DAYS * 24 * 60 * 60 * 1000L
        private const val REMINDER_COOLDOWN_MS = 24 * 60 * 60 * 1000L // Don't show more than once per day

        private const val NOTIFICATION_CHANNEL_ID = "profile_reminder_channel"
        private const val NOTIFICATION_ID = 3001
    }

    fun initialize(context: Context) {
        sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        createNotificationChannel(context)

        // Initialize timestamp if first time
        if (getLastProfileChangeTimestamp() == 0L) {
            recordProfileChange()
        }
    }

    /**
     * Record that user changed profile (resets the 21-day timer)
     */
    fun recordProfileChange() {
        sharedPrefs?.edit()
            ?.putLong(KEY_LAST_PROFILE_CHANGE, System.currentTimeMillis())
            ?.putInt(KEY_REMINDER_DISMISSED_COUNT, 0)
            ?.apply()
        Log.i(TAG, "Profile change recorded, 21-day timer reset")
    }

    /**
     * Check if reminder should be shown
     */
    fun shouldShowReminder(): Boolean {
        val lastChange = getLastProfileChangeTimestamp()
        val lastReminder = getLastReminderShownTimestamp()
        val now = System.currentTimeMillis()

        val daysSinceChange = (now - lastChange) / (24 * 60 * 60 * 1000L)
        val hoursSinceLastReminder = (now - lastReminder) / (60 * 60 * 1000L)

        val shouldShow = daysSinceChange >= REMINDER_INTERVAL_DAYS && hoursSinceLastReminder >= 24

        if (shouldShow) {
            Log.i(TAG, "Reminder should be shown: $daysSinceChange days since last profile change")
        }

        return shouldShow
    }

    /**
     * Get days since last profile change
     */
    fun getDaysSinceLastChange(): Int {
        val lastChange = getLastProfileChangeTimestamp()
        val now = System.currentTimeMillis()
        return ((now - lastChange) / (24 * 60 * 60 * 1000L)).toInt()
    }

    /**
     * Get days until next reminder
     */
    fun getDaysUntilReminder(): Int {
        val daysSinceChange = getDaysSinceLastChange()
        val daysRemaining = REMINDER_INTERVAL_DAYS - daysSinceChange
        return maxOf(0, daysRemaining)
    }

    /**
     * Show notification reminder
     */
    fun showReminderNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_profile_selection", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔄 Time to Change Your Music Profile!")
            .setContentText("It's been 21 days. Change your profile to avoid earworm!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's been 21 days since you last changed your music profile. " +
                        "Switching to a new profile helps keep your music fresh and prevents songs from getting stuck in your head. " +
                        "Tap to choose a new profile!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        recordReminderShown()
        Log.i(TAG, "Reminder notification shown")
    }

    /**
     * Record that reminder was shown
     */
    private fun recordReminderShown() {
        val dismissCount = sharedPrefs?.getInt(KEY_REMINDER_DISMISSED_COUNT, 0) ?: 0
        sharedPrefs?.edit()
            ?.putLong(KEY_LAST_REMINDER_SHOWN, System.currentTimeMillis())
            ?.putInt(KEY_REMINDER_DISMISSED_COUNT, dismissCount + 1)
            ?.apply()
    }

    /**
     * Dismiss reminder (user acknowledged but didn't change profile)
     */
    fun dismissReminder() {
        recordReminderShown()
        Log.i(TAG, "Reminder dismissed by user")
    }

    private fun getLastProfileChangeTimestamp(): Long {
        return sharedPrefs?.getLong(KEY_LAST_PROFILE_CHANGE, 0L) ?: 0L
    }

    private fun getLastReminderShownTimestamp(): Long {
        return sharedPrefs?.getLong(KEY_LAST_REMINDER_SHOWN, 0L) ?: 0L
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Profile Change Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you to change music profiles every 21 days"
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Get reminder statistics for display
     */
    fun getReminderStats(): ReminderStats {
        return ReminderStats(
            daysSinceLastChange = getDaysSinceLastChange(),
            daysUntilReminder = getDaysUntilReminder(),
            remindersDismissed = sharedPrefs?.getInt(KEY_REMINDER_DISMISSED_COUNT, 0) ?: 0
        )
    }
}

/**
 * Statistics about reminders
 */
data class ReminderStats(
    val daysSinceLastChange: Int,
    val daysUntilReminder: Int,
    val remindersDismissed: Int
)