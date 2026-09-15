plugins {
    alias(libs.plugins.android.library)
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
    implementation("net.java.dev.jna:jna:5.19.1@aar")

    implementation(libs.retrofit)
    implementation(libs.converter.scalars)
}