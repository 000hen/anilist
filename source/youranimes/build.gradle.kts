plugins {
    alias(libs.plugins.android.library)
    kotlin("plugin.serialization") version "2.0.0"
}

android {
    namespace = "one.muisnowdevs.apps.anilist.source.youranimes"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation(project(":source"))

    implementation(libs.retrofit)
    implementation(libs.converter.scalars)
    implementation(libs.kotlinx.serialization.json)
}