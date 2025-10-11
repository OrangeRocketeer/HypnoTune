package com.samsung.health.hrdatatransfer.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.health.data.TrackedData
import com.samsung.health.hrdatatransfer.data.AccelerometerRepository
import com.samsung.health.hrdatatransfer.data.ConnectionMessage
import com.samsung.health.hrdatatransfer.data.TrackerMessage
import com.samsung.health.hrdatatransfer.domain.AreTrackingCapabilitiesAvailableUseCase
import com.samsung.health.hrdatatransfer.domain.AutoSendDataUseCase
import com.samsung.health.hrdatatransfer.domain.MakeConnectionToHealthTrackingServiceUseCase
import com.samsung.health.hrdatatransfer.domain.SendMessageUseCase
import com.samsung.health.hrdatatransfer.domain.StopTrackingUseCase
import com.samsung.health.hrdatatransfer.domain.TrackHeartRateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val makeConnectionToHealthTrackingServiceUseCase: MakeConnectionToHealthTrackingServiceUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val stopTrackingUseCase: StopTrackingUseCase,
    private val areTrackingCapabilitiesAvailableUseCase: AreTrackingCapabilitiesAvailableUseCase,
    private val accelerometerRepository: AccelerometerRepository,
    private val autoSendDataUseCase: AutoSendDataUseCase
) : ViewModel() {

    private val _messageSentToast = MutableSharedFlow<Boolean>()
    val messageSentToast = _messageSentToast.asSharedFlow()

    private val _trackingState =
        MutableStateFlow(
            TrackingState(
                trackingRunning = false,
                trackingError = false,
                valueHR = "-",
                valueIBI = arrayListOf(),
                accelX = 0f,
                accelY = 0f,
                accelZ = 0f,
                message = "",
                autoSendEnabled = false,
                lastSendTime = ""
            )
        )
    val trackingState: StateFlow<TrackingState> = _trackingState

    private val _connectionState =
        MutableStateFlow(ConnectionState(connected = false, message = "", null))
    val connectionState: StateFlow<ConnectionState> = _connectionState

    @Inject
    lateinit var trackHeartRateUseCase: TrackHeartRateUseCase

    private var currentHR = "-"
    private var currentIBI = ArrayList<Int>(4)
    private var currentAccelX = 0f
    private var currentAccelY = 0f
    private var currentAccelZ = 0f

    private var trackingJob: Job? = null
    private var accelerometerJob: Job? = null
    private var autoSendJob: Job? = null

    fun stopTracking() {
        stopTrackingUseCase()
        trackingJob?.cancel()
        accelerometerJob?.cancel()
        autoSendJob?.cancel()
        accelerometerRepository.stopTracking()
        _trackingState.value = TrackingState(
            trackingRunning = false,
            trackingError = false,
            valueHR = "-",
            valueIBI = arrayListOf(),
            accelX = 0f,
            accelY = 0f,
            accelZ = 0f,
            message = "",
            autoSendEnabled = false,
            lastSendTime = ""
        )
    }

    fun setUpTracking() {
        Log.i(TAG, "setUpTracking()")
        viewModelScope.launch {
            makeConnectionToHealthTrackingServiceUseCase().collect { connectionMessage ->
                Log.i(TAG, "makeConnectionToHealthTrackingServiceUseCase().collect")
                when (connectionMessage) {
                    is ConnectionMessage.ConnectionSuccessMessage -> {
                        Log.i(TAG, "ConnectionMessage.ConnectionSuccessMessage")
                        _connectionState.value = ConnectionState(
                            connected = true,
                            message = "Connected to Health Tracking Service",
                            connectionException = null
                        )
                    }

                    is ConnectionMessage.ConnectionFailedMessage -> {
                        Log.i(TAG, "Connection: Sth went wrong")
                        _connectionState.value = ConnectionState(
                            connected = false,
                            message = "Connection to Health Tracking Service failed",
                            connectionException = connectionMessage.exception
                        )
                    }

                    is ConnectionMessage.ConnectionEndedMessage -> {
                        Log.i(TAG, "Connection ended")
                        _connectionState.value = ConnectionState(
                            connected = false,
                            message = "Connection ended. Try again later",
                            connectionException = null
                        )
                    }
                }
            }
        }
    }

    fun sendMessage() {
        viewModelScope.launch {
            if (sendMessageUseCase()) {
                _messageSentToast.emit(true)
                updateLastSendTime()
            } else {
                _messageSentToast.emit(false)
            }
        }
    }

    private fun processExerciseUpdate(trackedData: TrackedData) {
        val hr = trackedData.hr
        val ibi = trackedData.ibi
        Log.i(TAG, "last HeartRate: $hr, last IBI: $ibi")
        currentHR = hr.toString()
        currentIBI = ibi

        _trackingState.value = _trackingState.value.copy(
            trackingRunning = true,
            trackingError = false,
            valueHR = if (hr > 0) hr.toString() else "-",
            valueIBI = ibi,
            accelX = currentAccelX,
            accelY = currentAccelY,
            accelZ = currentAccelZ,
            message = ""
        )
    }

    fun startTracking() {
        trackingJob?.cancel()
        accelerometerJob?.cancel()
        autoSendJob?.cancel()

        Log.i(TAG, "trackHeartRate()")

        // Start accelerometer tracking
        startAccelerometerTracking()

        // Start auto-send (every 5 seconds)
        startAutoSend()

        if (areTrackingCapabilitiesAvailableUseCase()) {
            trackingJob = viewModelScope.launch {
                trackHeartRateUseCase().collect { trackerMessage ->
                    when (trackerMessage) {
                        is TrackerMessage.DataMessage -> {
                            // Add current accelerometer data to the tracked data
                            val trackedData = trackerMessage.trackedData
                            trackedData.accelX = currentAccelX
                            trackedData.accelY = currentAccelY
                            trackedData.accelZ = currentAccelZ

                            processExerciseUpdate(trackedData)
                            Log.i(TAG, "TrackerMessage.DataReceivedMessage")
                        }

                        is TrackerMessage.FlushCompletedMessage -> {
                            Log.i(TAG, "TrackerMessage.FlushCompletedMessage")
                            _trackingState.value = TrackingState(
                                trackingRunning = false,
                                trackingError = false,
                                valueHR = "-",
                                valueIBI = arrayListOf(),
                                accelX = 0f,
                                accelY = 0f,
                                accelZ = 0f,
                                message = "",
                                autoSendEnabled = false,
                                lastSendTime = ""
                            )
                        }

                        is TrackerMessage.TrackerErrorMessage -> {
                            Log.i(TAG, "TrackerMessage.TrackerErrorMessage")
                            _trackingState.value = TrackingState(
                                trackingRunning = false,
                                trackingError = true,
                                valueHR = "-",
                                valueIBI = arrayListOf(),
                                accelX = 0f,
                                accelY = 0f,
                                accelZ = 0f,
                                message = trackerMessage.trackerError,
                                autoSendEnabled = false,
                                lastSendTime = ""
                            )
                        }

                        is TrackerMessage.TrackerWarningMessage -> {
                            Log.i(TAG, "TrackerMessage.TrackerWarningMessage")
                            _trackingState.value = _trackingState.value.copy(
                                trackingRunning = true,
                                trackingError = false,
                                valueHR = "-",
                                valueIBI = currentIBI,
                                accelX = currentAccelX,
                                accelY = currentAccelY,
                                accelZ = currentAccelZ,
                                message = trackerMessage.trackerWarning
                            )
                        }
                    }
                }
            }
        } else {
            _trackingState.value = TrackingState(
                trackingRunning = false,
                trackingError = true,
                valueHR = "-",
                valueIBI = arrayListOf(),
                accelX = 0f,
                accelY = 0f,
                accelZ = 0f,
                message = "HR tracking capabilities not available",
                autoSendEnabled = false,
                lastSendTime = ""
            )
        }
    }

    private fun startAccelerometerTracking() {
        accelerometerJob = viewModelScope.launch {
            accelerometerRepository.startTracking().collect { accelData ->
                currentAccelX = accelData.x
                currentAccelY = accelData.y
                currentAccelZ = accelData.z

                Log.d(TAG, "Accelerometer updated: X=${accelData.x}, Y=${accelData.y}, Z=${accelData.z}")

                // Update UI state with new accelerometer values
                _trackingState.value = _trackingState.value.copy(
                    accelX = accelData.x,
                    accelY = accelData.y,
                    accelZ = accelData.z
                )
            }
        }
    }

    private fun startAutoSend() {
        autoSendJob = viewModelScope.launch {
            // Update state to show auto-send is enabled
            _trackingState.value = _trackingState.value.copy(autoSendEnabled = true)

            autoSendDataUseCase(intervalMillis = 5000).collect { success ->
                if (success) {
                    Log.i(TAG, "Auto-send successful")
                    updateLastSendTime()
                } else {
                    Log.i(TAG, "Auto-send failed or no data available yet")
                }
            }
        }
    }

    private fun updateLastSendTime() {
        val currentTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        _trackingState.value = _trackingState.value.copy(lastSendTime = currentTime)
    }
}

data class ConnectionState(
    val connected: Boolean,
    val message: String,
    val connectionException: HealthTrackerException?
)

data class TrackingState(
    val trackingRunning: Boolean,
    val trackingError: Boolean,
    val valueHR: String,
    val valueIBI: ArrayList<Int>,
    val accelX: Float,
    val accelY: Float,
    val accelZ: Float,
    val message: String,
    val autoSendEnabled: Boolean,
    val lastSendTime: String
)