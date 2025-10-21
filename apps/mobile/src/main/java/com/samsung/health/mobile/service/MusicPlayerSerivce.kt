package com.samsung.health.mobile.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.samsung.health.mobile.R
import com.samsung.health.mobile.data.StageConfigManager
import com.samsung.health.mobile.data.StageManager
import com.samsung.health.mobile.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

private const val TAG = "MusicPlayerService"

@AndroidEntryPoint
class MusicPlayerService : Service() {

    @Inject
    lateinit var stageManager: StageManager

    @Inject
    lateinit var stageConfigManager: StageConfigManager

    private var mediaPlayer: MediaPlayer? = null
    private var endTime: Long = 0
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var monitorJob: Job? = null
    private var stagePredictionReceiver: BroadcastReceiver? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID = "music_player_channel"

        const val EXTRA_MUSIC_URI = "musicUri"
        const val EXTRA_END_TIME = "endTime"
        const val EXTRA_START_RECORDING = "startRecording"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Register stage prediction receiver
        stagePredictionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val predictedStage = intent?.getIntExtra("predicted_stage", -1) ?: -1
                val confidence = intent?.getFloatExtra("confidence", 0f) ?: 0f

                if (predictedStage in 0..4) {
                    Log.i(TAG, "Received stage prediction: $predictedStage (confidence: ${"%.2f".format(confidence)})")
                    stageManager.transitionToStage(
                        stageNumber = predictedStage,
                        mediaPlayer = mediaPlayer,
                        onTransitionComplete = { config ->
                            Log.i(TAG, "Transitioned to ${config.stageName}")
                            // Update notification with current stage info
                            startForeground(NOTIFICATION_ID, buildNotification(true, config.stageName))
                        },
                        onMusicSwitchNeeded = { uri ->
                            switchMusic(uri, predictedStage)
                        }
                    )
                }
            }
        }

        val filter = IntentFilter("com.samsung.health.mobile.STAGE_PREDICTION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stagePredictionReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            androidx.core.content.ContextCompat.registerReceiver(
                this,
                stagePredictionReceiver,
                filter,
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        Log.i(TAG, "MusicPlayerService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: action=${intent?.action}")

        when (intent?.action) {
            ACTION_START -> {
                val musicUri = intent.getStringExtra(EXTRA_MUSIC_URI)
                endTime = intent.getLongExtra(EXTRA_END_TIME, 0L)
                val startRecording = intent.getBooleanExtra(EXTRA_START_RECORDING, false)

                if (musicUri != null && endTime != 0L) {
                    startMusic(Uri.parse(musicUri), startRecording)
                }
            }
            ACTION_PAUSE -> pauseMusic()
            ACTION_RESUME -> resumeMusic()
            ACTION_STOP -> stopMusic()
        }

        return START_STICKY
    }

    private fun startMusic(uri: Uri, startRecording: Boolean) {
        try {
            // Release existing player if any
            mediaPlayer?.release()

            // Check if Stage 0 has a specific music file configured
            val stage0Config = stageConfigManager.getStage(0)
            val musicToPlay = if (stage0Config?.musicUri != null) {
                Uri.parse(stage0Config.musicUri)
            } else {
                uri // Use the URI passed from the schedule/play now action
            }

            // Create and configure media player
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, musicToPlay)
                isLooping = true
                prepare()
            }

            // Initialize to Stage 0 (Rest) with its configured volume
            stageManager.initializeStage(0, mediaPlayer)

            // Now start playback
            mediaPlayer?.start()

            // Start foreground with notification
            val stageName = stage0Config?.stageName ?: "Rest"
            startForeground(NOTIFICATION_ID, buildNotification(true, stageName))

            // Broadcast that music started (for recording to start)
            if (startRecording) {
                sendBroadcast(Intent("com.samsung.health.mobile.MUSIC_STARTED"))
            }

            // Monitor end time
            startEndTimeMonitor()

            Log.i(TAG, "Music started successfully in Stage 0 with music: $musicToPlay")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting music", e)
            stopSelf()
        }
    }

    private fun switchMusic(uri: Uri, newStage: Int) {
        try {
            val wasPlaying = mediaPlayer?.isPlaying ?: false
            val currentPosition = mediaPlayer?.currentPosition ?: 0

            Log.i(TAG, "Switching music to: $uri for stage $newStage")

            // Stop current playback
            mediaPlayer?.stop()
            mediaPlayer?.release()

            // Create new media player with new music
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                isLooping = true
                prepare()
            }

            // Get target volume for the new stage
            val newStageConfig = stageConfigManager.getStage(newStage)
            val targetVolume = newStageConfig?.targetVolume ?: 50

            // Set volume immediately
            stageManager.updateVolumeAfterMusicSwitch(mediaPlayer, targetVolume)

            // Start playback if it was playing before
            if (wasPlaying) {
                mediaPlayer?.start()
            }

            // Update notification
            val stageName = newStageConfig?.stageName ?: "Stage $newStage"
            startForeground(NOTIFICATION_ID, buildNotification(wasPlaying, stageName))

            Log.i(TAG, "Successfully switched to new music at ${targetVolume}% volume")
        } catch (e: Exception) {
            Log.e(TAG, "Error switching music", e)
            // Try to recover by restarting with default stage 0 music
            try {
                val stage0Config = stageConfigManager.getStage(0)
                if (stage0Config?.musicUri != null) {
                    startMusic(Uri.parse(stage0Config.musicUri), false)
                }
            } catch (recoveryException: Exception) {
                Log.e(TAG, "Failed to recover from music switch error", recoveryException)
                stopSelf()
            }
        }
    }

    private fun pauseMusic() {
        mediaPlayer?.pause()
        val currentStage = stageManager.getCurrentStage()
        val stageName = stageConfigManager.getStage(currentStage)?.stageName ?: "Stage $currentStage"
        startForeground(NOTIFICATION_ID, buildNotification(false, stageName))
        Log.i(TAG, "Music paused")
    }

    private fun resumeMusic() {
        mediaPlayer?.start()
        val currentStage = stageManager.getCurrentStage()
        val stageName = stageConfigManager.getStage(currentStage)?.stageName ?: "Stage $currentStage"
        startForeground(NOTIFICATION_ID, buildNotification(true, stageName))
        Log.i(TAG, "Music resumed")
    }

    private fun stopMusic() {
        Log.i(TAG, "Stopping music")

        monitorJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        // Broadcast that music stopped (for recording to stop)
        sendBroadcast(Intent("com.samsung.health.mobile.MUSIC_STOPPED"))

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startEndTimeMonitor() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            while (isActive) {
                if (System.currentTimeMillis() >= endTime) {
                    Log.i(TAG, "End time reached, stopping music")
                    stopMusic()
                    break
                }
                delay(1000) // Check every second
            }
        }
    }

    private fun buildNotification(isPlaying: Boolean, stageName: String = "Rest"): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Play/Pause action
        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "Pause",
                getPendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                "Play",
                getPendingIntent(ACTION_RESUME)
            )
        }

        // Stop action
        val stopAction = NotificationCompat.Action(
            android.R.drawable.ic_delete,
            "Stop",
            getPendingIntent(ACTION_STOP)
        )

        // Get current volume percentage
        val currentVolume = stageManager.getCurrentVolumePercent()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player - $stageName")
            .setContentText(if (isPlaying) "Playing at $currentVolume% volume" else "Paused")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .addAction(playPauseAction)
            .addAction(stopAction)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicPlayerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows music playback controls and stage info"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getMediaPlayer(): MediaPlayer? = mediaPlayer

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "MusicPlayerService destroyed")

        // Unregister stage prediction receiver
        stagePredictionReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
            }
        }

        monitorJob?.cancel()
        mediaPlayer?.release()
        serviceScope.cancel()
        stageManager.cleanup()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}