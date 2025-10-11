// Declares this is an Android library and uses Kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    // This plugin is required for parsing the JSON stats file
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

// Basic Android project configuration
android {
    // This MUST match your package name structure.
    namespace = "com.samsung.health.mobile.sleepclassifier"

    // IMPORTANT: Match these versions with the ones in 'projects/mobile/build.gradle'
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

// The dependencies your module needs to function
dependencies {
    // For ONNX Runtime on Android
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.1")

    // For Kotlin Coroutines (for background processing)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // For parsing the JSON stats file
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}