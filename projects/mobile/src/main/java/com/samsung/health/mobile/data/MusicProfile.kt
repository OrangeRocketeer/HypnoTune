package com.samsung.health.mobile.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class representing a complete music profile
 */
data class MusicProfile(
    val id: String,
    val name: String,
    val description: String,
    val stages: List<StageConfig>,
    val isCustom: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)

/**
 * Manager for music profiles with pre-built and custom profiles
 */
@Singleton
class MusicProfileManager @Inject constructor(
    private val stageConfigManager: StageConfigManager
) {

    private val gson = Gson()
    private var sharedPrefs: SharedPreferences? = null

    companion object {
        private const val PREFS_NAME = "music_profile_prefs"
        private const val KEY_CURRENT_PROFILE_ID = "current_profile_id"
        private const val KEY_CUSTOM_PROFILES = "custom_profiles"

        // Pre-built profile IDs
        const val PROFILE_BALANCED = "balanced"
        const val PROFILE_ENERGETIC = "energetic"
        const val PROFILE_CALM = "calm"
        const val PROFILE_WORKOUT = "workout"
        const val PROFILE_STUDY = "study"
        const val PROFILE_CUSTOM = "custom"
    }

    fun initialize(context: Context) {
        sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Get all available profiles (pre-built + custom)
     */
    fun getAllProfiles(): List<MusicProfile> {
        val preBuiltProfiles = getPreBuiltProfiles()
        val customProfiles = getCustomProfiles()
        return preBuiltProfiles + customProfiles
    }

    /**
     * Pre-built profiles with different characteristics
     */
    fun getPreBuiltProfiles(): List<MusicProfile> = listOf(
        // BALANCED PROFILE - Moderate progression
        MusicProfile(
            id = PROFILE_BALANCED,
            name = "Balanced",
            description = "Smooth volume transitions for everyday activities",
            stages = listOf(
                StageConfig(
                    stageNumber = 0,
                    stageName = "Rest",
                    targetVolume = 30,
                    musicType = "Ambient/Calm",
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
                    musicType = "Upbeat",
                    fadeDuration = 2500,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 3,
                    stageName = "High Activity",
                    targetVolume = 80,
                    musicType = "Energetic",
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
            ),
            isCustom = false
        ),

        // ENERGETIC PROFILE - Higher volumes, faster transitions
        MusicProfile(
            id = PROFILE_ENERGETIC,
            name = "Energetic",
            description = "Louder volumes and quick transitions for high-energy workouts",
            stages = listOf(
                StageConfig(
                    stageNumber = 0,
                    stageName = "Warm-up",
                    targetVolume = 40,
                    musicType = "Upbeat Pop",
                    fadeDuration = 1000,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 1,
                    stageName = "Building Up",
                    targetVolume = 60,
                    musicType = "Dance",
                    fadeDuration = 1500,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 2,
                    stageName = "Active",
                    targetVolume = 75,
                    musicType = "EDM",
                    fadeDuration = 1500,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 3,
                    stageName = "High Intensity",
                    targetVolume = 90,
                    musicType = "Hard Rock",
                    fadeDuration = 1000,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 4,
                    stageName = "Maximum Power",
                    targetVolume = 100,
                    musicType = "Metal/Dubstep",
                    fadeDuration = 500,
                    musicUri = null
                )
            ),
            isCustom = false
        ),

        // CALM PROFILE - Lower volumes, slower transitions
        MusicProfile(
            id = PROFILE_CALM,
            name = "Calm & Focus",
            description = "Gentle volumes and slow transitions for relaxation and focus",
            stages = listOf(
                StageConfig(
                    stageNumber = 0,
                    stageName = "Deep Rest",
                    targetVolume = 15,
                    musicType = "Nature Sounds",
                    fadeDuration = 4000,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 1,
                    stageName = "Light Movement",
                    targetVolume = 25,
                    musicType = "Acoustic",
                    fadeDuration = 4000,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 2,
                    stageName = "Gentle Activity",
                    targetVolume = 40,
                    musicType = "Lo-Fi",
                    fadeDuration = 3500,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 3,
                    stageName = "Active Relaxation",
                    targetVolume = 55,
                    musicType = "Chill Pop",
                    fadeDuration = 3000,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 4,
                    stageName = "Energized Focus",
                    targetVolume = 70,
                    musicType = "Soft Electronic",
                    fadeDuration = 2500,
                    musicUri = null
                )
            ),
            isCustom = false
        ),

        // WORKOUT PROFILE - Optimized for gym sessions
        MusicProfile(
            id = PROFILE_WORKOUT,
            name = "Workout Beast",
            description = "Pumped-up volumes perfect for intense gym sessions",
            stages = listOf(
                StageConfig(
                    stageNumber = 0,
                    stageName = "Stretching",
                    targetVolume = 35,
                    musicType = "Motivational",
                    fadeDuration = 2000,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 1,
                    stageName = "Cardio Warm-up",
                    targetVolume = 65,
                    musicType = "Hip-Hop",
                    fadeDuration = 1500,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 2,
                    stageName = "Strength Training",
                    targetVolume = 80,
                    musicType = "Rock/Rap",
                    fadeDuration = 1000,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 3,
                    stageName = "High Intensity",
                    targetVolume = 92,
                    musicType = "Heavy Bass",
                    fadeDuration = 800,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 4,
                    stageName = "Beast Mode",
                    targetVolume = 100,
                    musicType = "Aggressive EDM",
                    fadeDuration = 500,
                    musicUri = null
                )
            ),
            isCustom = false
        ),

        // STUDY PROFILE - Minimal distraction
        MusicProfile(
            id = PROFILE_STUDY,
            name = "Study Focus",
            description = "Low, consistent volumes to maintain concentration",
            stages = listOf(
                StageConfig(
                    stageNumber = 0,
                    stageName = "Deep Focus",
                    targetVolume = 20,
                    musicType = "Classical",
                    fadeDuration = 5000,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 1,
                    stageName = "Light Focus",
                    targetVolume = 25,
                    musicType = "Piano",
                    fadeDuration = 4500,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 2,
                    stageName = "Active Study",
                    targetVolume = 30,
                    musicType = "Instrumental",
                    fadeDuration = 4000,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 3,
                    stageName = "Break Time",
                    targetVolume = 45,
                    musicType = "Light Jazz",
                    fadeDuration = 3000,
                    musicUri = null
                ),
                StageConfig(
                    stageNumber = 4,
                    stageName = "Power Study",
                    targetVolume = 50,
                    musicType = "Lo-Fi Beats",
                    fadeDuration = 2500,
                    musicUri = null
                )
            ),
            isCustom = false
        )
    )

    /**
     * Get saved custom profiles
     */
    fun getCustomProfiles(): List<MusicProfile> {
        val json = sharedPrefs?.getString(KEY_CUSTOM_PROFILES, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<MusicProfile>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Get currently selected profile
     */
    fun getCurrentProfile(): MusicProfile {
        val currentId = sharedPrefs?.getString(KEY_CURRENT_PROFILE_ID, PROFILE_BALANCED)
            ?: PROFILE_BALANCED

        // Try to find in all profiles
        return getAllProfiles().find { it.id == currentId }
            ?: getPreBuiltProfiles().first() // Fallback to Balanced
    }

    /**
     * Get current profile ID
     */
    fun getCurrentProfileId(): String {
        return sharedPrefs?.getString(KEY_CURRENT_PROFILE_ID, PROFILE_BALANCED)
            ?: PROFILE_BALANCED
    }

    /**
     * Apply a profile (loads its stages into the active configuration)
     */
    fun applyProfile(profile: MusicProfile) {
        // Save stages to StageConfigManager
        stageConfigManager.saveStages(profile.stages)

        // Set as current profile
        sharedPrefs?.edit()
            ?.putString(KEY_CURRENT_PROFILE_ID, profile.id)
            ?.apply()
    }

    /**
     * Save current configuration as a custom profile
     */
    fun saveAsCustomProfile(name: String, description: String): MusicProfile {
        val currentStages = stageConfigManager.getStages()
        val customProfile = MusicProfile(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            description = description,
            stages = currentStages,
            isCustom = true,
            createdTimestamp = System.currentTimeMillis()
        )

        // Add to custom profiles list
        val existingCustom = getCustomProfiles().toMutableList()
        existingCustom.add(customProfile)
        saveCustomProfiles(existingCustom)

        // Set as current profile
        applyProfile(customProfile)

        return customProfile
    }

    /**
     * Save a custom profile (for internal use and auto-save)
     * This is used when modifying existing profiles
     */
    fun saveCustomProfile(profile: MusicProfile) {
        val customProfiles = getCustomProfiles().toMutableList()

        // Check if profile already exists (by ID)
        val existingIndex = customProfiles.indexOfFirst { it.id == profile.id }
        if (existingIndex != -1) {
            // Update existing profile
            customProfiles[existingIndex] = profile
            android.util.Log.d("MusicProfileManager", "Updated existing custom profile: ${profile.name}")
        } else {
            // Add new profile
            customProfiles.add(profile)
            android.util.Log.d("MusicProfileManager", "Added new custom profile: ${profile.name}")
        }

        saveCustomProfiles(customProfiles)

        // Set as current profile
        sharedPrefs?.edit()
            ?.putString(KEY_CURRENT_PROFILE_ID, profile.id)
            ?.apply()
    }

    /**
     * Delete a custom profile
     */
    fun deleteCustomProfile(profileId: String) {
        val customProfiles = getCustomProfiles().toMutableList()
        customProfiles.removeAll { it.id == profileId }
        saveCustomProfiles(customProfiles)

        // If deleted profile was current, switch to Balanced
        if (getCurrentProfileId() == profileId) {
            applyProfile(getPreBuiltProfiles().first())
        }
    }

    /**
     * Update an existing custom profile
     */
    fun updateCustomProfile(updatedProfile: MusicProfile) {
        val customProfiles = getCustomProfiles().toMutableList()
        val index = customProfiles.indexOfFirst { it.id == updatedProfile.id }
        if (index != -1) {
            customProfiles[index] = updatedProfile
            saveCustomProfiles(customProfiles)
            android.util.Log.d("MusicProfileManager", "Updated custom profile via updateCustomProfile: ${updatedProfile.name}")
        }
    }

    /**
     * Save custom profiles to SharedPreferences
     */
    private fun saveCustomProfiles(profiles: List<MusicProfile>) {
        val json = gson.toJson(profiles)
        sharedPrefs?.edit()?.putString(KEY_CUSTOM_PROFILES, json)?.apply()
    }

    /**
     * Check if a profile is currently active
     */
    fun isProfileActive(profileId: String): Boolean {
        return getCurrentProfileId() == profileId
    }

    /**
     * Get profile by ID
     */
    fun getProfileById(profileId: String): MusicProfile? {
        return getAllProfiles().find { it.id == profileId }
    }
}