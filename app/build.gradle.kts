import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun escapeBuildConfig(value: String): String =
    value.replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "com.demo.jsontoview"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.demo.jsontoview"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Release and default: empty key (never ship secrets).
        buildConfigField("String", "XAI_API_KEY", "\"\"")
        buildConfigField("String", "XAI_BASE_URL", "\"https://api.x.ai/v1\"")
        buildConfigField("String", "XAI_MODEL", "\"grok-4.5\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            // Demo only: read from gitignored local.properties
            val key = localProperties.getProperty("XAI_API_KEY", "")
            buildConfigField("String", "XAI_API_KEY", "\"${escapeBuildConfig(key)}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "XAI_API_KEY", "\"\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":json-to-view"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.glide)
    implementation(libs.okhttp)
    implementation(libs.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
