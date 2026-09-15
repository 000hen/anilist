import java.util.Properties

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

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDir(
                layout.buildDirectory
                    .dir("rustJniLibs")
                    .get()
                    .asFile
            )
        }
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

val rustProjectDir =
    rootDir.resolve("rust/anilist-rs").canonicalFile

val cargoCrate = "anilist-ffi"

val cargoNdkAbis = listOf(
    "arm64-v8a",
    "armeabi-v7a",
    "x86_64",
    "x86",
)

val rustJniLibsDir =
    layout.buildDirectory.dir("rustJniLibs")

fun resolveAndroidNdkHome(): String {
    System.getenv("ANDROID_NDK_HOME")?.let {
        return it
    }

    val sdkDir = Properties().apply {
        rootProject
            .file("local.properties")
            .takeIf { it.exists() }
            ?.inputStream()
            ?.use { load(it) }
    }.getProperty("sdk.dir")
        ?: System.getenv("ANDROID_HOME")
        ?: System.getenv("ANDROID_SDK_ROOT")
        ?: error(
            "Cannot locate the Android SDK: " +
                    "set sdk.dir in local.properties or ANDROID_HOME."
        )

    val ndkRoot = File(sdkDir, "ndk")

    val installed = ndkRoot
        .listFiles { file -> file.isDirectory }
        ?.takeIf { it.isNotEmpty() }
        ?: error(
            "No NDK found under $ndkRoot. " +
                    "Install one via the SDK Manager."
        )

    fun version(directory: File): List<Int> =
        directory.name
            .split(".")
            .map { it.toIntOrNull() ?: 0 }

    return installed.maxWithOrNull(
        compareBy(
            { version(it).getOrElse(0) { 0 } },
            { version(it).getOrElse(1) { 0 } },
            { version(it).getOrElse(2) { 0 } },
        )
    )!!.absolutePath
}

val cargoNdkBuild = tasks.register<Exec>("cargoNdkBuild") {
    val outputDir = rustJniLibsDir.get().asFile
    inputs.files(
        fileTree(rustProjectDir) {
            include("**/*.rs")
            include("**/Cargo.toml")
            include("Cargo.lock")

            exclude("target/**")
        }
    )

    outputs.dir(rustJniLibsDir)
    workingDir = rustProjectDir

    environment("ANDROID_NDK_HOME", resolveAndroidNdkHome())
    val cargoBin =
        File(
            System.getProperty("user.home"),
            ".cargo/bin",
        ).absolutePath

    environment(
        "PATH",
        "$cargoBin${File.pathSeparator}${System.getenv("PATH")}",
    )

    doFirst { outputDir.mkdirs() }
    commandLine(
        listOf("cargo", "ndk") +
                cargoNdkAbis.flatMap {
                    listOf("-t", it)
                } +
                listOf(
                    "-o",
                    outputDir.absolutePath,
                    "build",
                    "--release",
                    "-p",
                    "anilist-ffi",
                    "--no-default-features",
                    "--features",
                    "youranimes",
                )
    )
}

tasks.matching {
    it.name.startsWith("merge") &&
            it.name.endsWith("JniLibFolders")
}.configureEach {
    dependsOn(cargoNdkBuild)
}