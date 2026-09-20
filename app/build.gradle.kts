plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val releaseSigningVariables = listOf(
    "RELEASE_KEYSTORE_FILE",
    "RELEASE_STORE_PASSWORD",
    "RELEASE_KEY_ALIAS",
    "RELEASE_KEY_PASSWORD",
)
val releaseSigningValues = releaseSigningVariables.associateWith {
    providers.environmentVariable(it).orNull
}
val configuredReleaseSigningValues = releaseSigningValues.filterValues { !it.isNullOrBlank() }
val buildPerAbiApks =
    providers.gradleProperty("releasePerAbi").map { it.toBoolean() }.getOrElse(false)

// A partial configuration must not quietly turn an intended signed release into an unsigned one.
require(configuredReleaseSigningValues.isEmpty() || configuredReleaseSigningValues.size == releaseSigningVariables.size) {
    "Release signing requires all of: ${releaseSigningVariables.joinToString()}"
}

android {
    namespace = "one.muisnowdevs.apps.anilist"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "one.muisnowdevs.apps.anilist"
        minSdk = 26
        targetSdk = 37
        versionCode = 8
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val releaseSigningConfig = if (configuredReleaseSigningValues.isNotEmpty()) {
        signingConfigs.create("release") {
            storeFile = file(configuredReleaseSigningValues.getValue("RELEASE_KEYSTORE_FILE")!!)
            storePassword = configuredReleaseSigningValues.getValue("RELEASE_STORE_PASSWORD")
            keyAlias = configuredReleaseSigningValues.getValue("RELEASE_KEY_ALIAS")
            keyPassword = configuredReleaseSigningValues.getValue("RELEASE_KEY_PASSWORD")
        }
    } else {
        null
    }

    buildTypes {
        release {
            signingConfig = releaseSigningConfig
            optimization {
                enable = true
                isMinifyEnabled = true
                isShrinkResources = true
            }
        }
    }
    splits {
        abi {
            // Splitting is opt-in so ordinary debug builds keep their conventional single APK.
            isEnable = buildPerAbiApks
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            isUniversalApk = false
        }
    }
    packaging {
        // JNA also ships obsolete ABIs for which this app has no Rust library.
        jniLibs.excludes += setOf("lib/armeabi/**", "lib/mips/**", "lib/mips64/**")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(project(":source"))
}
