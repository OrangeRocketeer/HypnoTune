package com.samsung.health.hrdatatransfer.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AccelerometerRepoImpl"

@Singleton
class AccelerometerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AccelerometerRepository {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var listener: SensorEventListener? = null

    override fun startTracking(): Flow<AccelerometerData> = callbackFlow {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            Log.e(TAG, "Accelerometer sensor not available")
            close()
            return@callbackFlow
        }

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val x = Math.abs(it.values[0])
                    val y = Math.abs(it.values[1])
                    val z = Math.abs(it.values[2])

                    Log.d(TAG, "Accelerometer: X=$x, Y=$y, Z=$z")
                    trySend(AccelerometerData(x, y, z))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not needed for this implementation
            }
        }

        sensorManager?.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        Log.i(TAG, "Accelerometer tracking started")

        awaitClose {
            Log.i(TAG, "Accelerometer tracking stopped")
            stopTracking()
        }
    }

    override fun stopTracking() {
        listener?.let {
            sensorManager?.unregisterListener(it)
        }
        listener = null
    }
}