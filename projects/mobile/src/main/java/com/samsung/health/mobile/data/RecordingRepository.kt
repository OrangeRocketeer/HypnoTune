package com.samsung.health.mobile.data

import android.content.Context
import com.samsung.health.data.TrackedData
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingRepository @Inject constructor(
    private val dataRecorder: DataRecorder
) {

    fun startRecording(context: Context): Boolean {
        return dataRecorder.startRecording(context)
    }

    fun stopRecording(): File? {
        return dataRecorder.stopRecording()
    }

    fun recordData(data: TrackedData) {
        dataRecorder.recordData(data)
    }

    fun isRecording(): Boolean {
        return dataRecorder.isRecording()
    }

    fun getCurrentFileName(): String? {
        return dataRecorder.getRecordingFileName()
    }
}