package com.yourapp.sleepclassifier.internal

import ai.onnxruntime.*
import android.content.Context
import java.nio.FloatBuffer

/**
 * Manages loading and running the ONNX model.
 * Should be treated as a singleton as initialization is expensive.
 */
internal class OnnxModelHandler(context: Context, modelFileName: String) {

    private val ortSession: OrtSession
    private val ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()

    init {
        val modelBytes = context.assets.open(modelFileName).readBytes()
        ortSession = ortEnv.createSession(modelBytes, OrtSession.SessionOptions())
    }

    /**
     * Runs inference on the normalized feature array.
     */
    fun predict(input: FloatArray): Pair<Int, Map<Int, Float>> {
        val inputName = ortSession.inputNames.first()
        val inputTensor = OnnxTensor.createTensor(
            ortEnv,
            FloatBuffer.wrap(input),
            longArrayOf(1, input.size.toLong()) // Shape: [1, num_features]
        )

        val results = ortSession.run(mapOf(inputName to inputTensor))
        
        // The output structure depends on the model conversion.
        // Usually for LightGBM, output 0 is the label, output 1 is the probabilities.
        val labelOutput = results[0] as OnnxTensor
        val predictedLabel = (labelOutput.value as LongArray)[0].toInt()

        val probabilitiesOutput = results[1].value as List<Map<Long, Float>>
        val confidenceMap = probabilitiesOutput.first().mapKeys { it.key.toInt() }

        return Pair(predictedLabel, confidenceMap)
    }

    fun close() {
        ortSession.close()
        ortEnv.close()
    }
}