package com.samsung.health.mobile.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class representing a single stage configuration
 */
data class StageConfig(
    val stageNumber: Int,
    val stageName: String,
    val targetVolume: Int, // 0-100
    val musicType: String, // Playlist name or music type
    val fadeDuration: Int, // in milliseconds
    val musicUri: String? = null // URI of selected music file for this stage
)

/**
 * Manager for stage configurations with persistent storage
 */
@Singleton
class StageConfigManager @Inject constructor() {

    private val gson = Gson()
    private var sharedPrefs: SharedPreferences? = null

    companion object {
        private const val PREFS_NAME = "stage_config_prefs"
        private const val KEY_STAGES = "stages"

        // Default stage configurations
        fun getDefaultStages(): List<StageConfig> = listOf(
            StageConfig(
                stageNumber = 0,
                stageName = "Rest",
                targetVolume = 30,
                musicType = "Calm/Ambient",
                fadeDuration = 2000,
                musicUri = null
            ),
            StageConfig(
                stageNumber = 1,
                stageName = "Light Activity",
                targetVolume = 50,
                musicType = "Soft Pop",
                fadeDuration = 3000,
                musicUri = null
            ),
            StageConfig(
                stageNumber = 2,
                stageName = "Moderate Activity",
                targetVolume = 65,
                musicType = "Upbeat Pop",
                fadeDuration = 2500,
                musicUri = null
            ),
            StageConfig(
                stageNumber = 3,
                stageName = "High Activity",
                targetVolume = 80,
                musicType = "Rock/EDM",
                fadeDuration = 2000,
                musicUri = null
            ),
            StageConfig(
                stageNumber = 4,
                stageName = "Peak Activity",
                targetVolume = 95,
                musicType = "High Energy",
                fadeDuration = 1500,
                musicUri = null
            )
        )
    }

    fun initialize(context: Context) {
        sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getStages(): List<StageConfig> {
        val json = sharedPrefs?.getString(KEY_STAGES, null)
        return if (json != null) {
            val type = object : TypeToken<List<StageConfig>>() {}.type
            gson.fromJson(json, type)
        } else {
            getDefaultStages()
        }
    }

    fun saveStages(stages: List<StageConfig>) {
        val json = gson.toJson(stages)
        sharedPrefs?.edit()?.putString(KEY_STAGES, json)?.apply()
    }

    fun updateStage(updatedStage: StageConfig) {
        val stages = getStages().toMutableList()
        val index = stages.indexOfFirst { it.stageNumber == updatedStage.stageNumber }
        if (index != -1) {
            stages[index] = updatedStage
            saveStages(stages)
        }
    }

    fun getStage(stageNumber: Int): StageConfig? {
        return getStages().find { it.stageNumber == stageNumber }
    }

    fun resetToDefaults() {
        saveStages(getDefaultStages())
    }

    /**
     * Update music URI for a specific stage
     */
    fun updateStageMusicUri(stageNumber: Int, musicUri: Uri?) {
        val stages = getStages().toMutableList()
        val index = stages.indexOfFirst { it.stageNumber == stageNumber }
        if (index != -1) {
            stages[index] = stages[index].copy(musicUri = musicUri?.toString())
            saveStages(stages)
        }
    }

    /**
     * Get music URI for a specific stage
     */
    fun getStageMusicUri(stageNumber: Int): Uri? {
        val stage = getStage(stageNumber)
        return stage?.musicUri?.let { Uri.parse(it) }
    }
}