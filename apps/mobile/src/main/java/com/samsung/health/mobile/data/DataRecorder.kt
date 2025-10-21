package com.samsung.health.mobile.data

import android.content.Context
import android.os.Environment
import android.util.Log
import com.samsung.health.data.TrackedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DataRecorder"

@Singleton
class DataRecorder @Inject constructor() {

    private var isRecording = false
    private var currentFile: File? = null
    private var fileWriter: FileWriter? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    // Store the latest values for each sensor type
    private var latestHeartRate: Int = 0
    private var latestIbiValues: List<Int> = emptyList()
    private var latestAccelX: Float = 0f
    private var latestAccelY: Float = 0f
    private var latestAccelZ: Float = 0f
    private var lastRecordedTimestamp: Long = 0L

    fun isRecording(): Boolean = isRecording

    fun startRecording(context: Context): Boolean {
        if (isRecording) {
            Log.w(TAG, "Recording already in progress")
            return false
        }

        return try {
            val fileName = "health_data_${fileNameFormat.format(Date())}.csv"
            val directory = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "HealthData"
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            currentFile = File(directory, fileName)
            fileWriter = FileWriter(currentFile, true)

            // Write CSV header
            fileWriter?.append("Timestamp,HeartRate,IBI_Values,AccelX,AccelY,AccelZ\n")
            fileWriter?.flush()

            isRecording = true
            lastRecordedTimestamp = 0L
            latestHeartRate = 0
            latestIbiValues = emptyList()
            latestAccelX = 0f
            latestAccelY = 0f
            latestAccelZ = 0f

            Log.i(TAG, "Started recording to: ${currentFile?.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            false
        }
    }

    fun recordData(data: TrackedData) {
        if (!isRecording || fileWriter == null) {
            return
        }

        try {
            val currentMillis = System.currentTimeMillis()

            // Update latest sensor values
            if (data.hr > 0) {
                latestHeartRate = data.hr
            }
            if (data.ibi.isNotEmpty()) {
                latestIbiValues = data.ibi
            }
            latestAccelX = data.accelX
            latestAccelY = data.accelY
            latestAccelZ = data.accelZ

            // Only write to file once per 5-second interval
            // Allow a small tolerance (4.5 seconds) to account for timing variations
            if (currentMillis - lastRecordedTimestamp >= 4500) {
                lastRecordedTimestamp = currentMillis

                val timestamp = dateFormat.format(Date(currentMillis))
                val heartRate = if (latestHeartRate > 0) latestHeartRate.toString() else ""
                val ibiValues = latestIbiValues.joinToString(";")
                val accelX = String.format("%.3f", latestAccelX)
                val accelY = String.format("%.3f", latestAccelY)
                val accelZ = String.format("%.3f", latestAccelZ)

                val line = "$timestamp,$heartRate,\"$ibiValues\",$accelX,$accelY,$accelZ\n"
                fileWriter?.append(line)
                fileWriter?.flush()

                Log.d(TAG, "Recorded data at $timestamp: HR=$heartRate, Accel=($accelX,$accelY,$accelZ)")
            } else {
                Log.d(TAG, "Updated sensor values, waiting for next 5-second interval")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error recording data", e)
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) {
            Log.w(TAG, "No recording in progress")
            return null
        }

        return try {
            fileWriter?.flush()
            fileWriter?.close()
            fileWriter = null
            isRecording = false
            lastRecordedTimestamp = 0L
            latestHeartRate = 0
            latestIbiValues = emptyList()
            latestAccelX = 0f
            latestAccelY = 0f
            latestAccelZ = 0f

            val file = currentFile
            currentFile = null

            Log.i(TAG, "Stopped recording. File saved: ${file?.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            null
        }
    }

    fun getRecordingFileName(): String? {
        return currentFile?.name
    }
}