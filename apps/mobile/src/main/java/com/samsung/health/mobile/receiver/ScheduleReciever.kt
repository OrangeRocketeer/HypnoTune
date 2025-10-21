package com.samsung.health.mobile.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.samsung.health.mobile.service.MusicPlayerService

private const val TAG = "ScheduleReceiver"

class ScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Received broadcast: ${intent.action}")

        if (intent.action == "com.samsung.health.mobile.START_MUSIC") {
            val musicUri = intent.getStringExtra(MusicPlayerService.EXTRA_MUSIC_URI)
            val endTime = intent.getLongExtra(MusicPlayerService.EXTRA_END_TIME, 0L)
            val startRecording = intent.getBooleanExtra(MusicPlayerService.EXTRA_START_RECORDING, false)

            if (musicUri != null && endTime != 0L) {
                val serviceIntent = Intent(context, MusicPlayerService::class.java).apply {
                    action = MusicPlayerService.ACTION_START
                    putExtra(MusicPlayerService.EXTRA_MUSIC_URI, musicUri)
                    putExtra(MusicPlayerService.EXTRA_END_TIME, endTime)
                    putExtra(MusicPlayerService.EXTRA_START_RECORDING, startRecording)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                Log.i(TAG, "Started MusicPlayerService")
            }
        }
    }
}