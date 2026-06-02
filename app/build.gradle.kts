import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.issueissyu.fe"
    compileSdk = 35

    val localProperties = Properties().apply {
        rootProject.file("local.properties").takeIf { it.exists() }?.reader()?.use(::load)
    }

    // local.properties → BuildConfig / manifestPlaceholders
    fun Properties.buildConfigString(
        primaryKey: String,
        fallbackKey: String? = null,
        default: String = ""
    ): String =
        (getProperty(primaryKey) ?: fallbackKey?.let(::getProperty) ?: default).trim()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")

    defaultConfig {
        applicationId = "com.issueissyu.fe"
        minSdk = 26
        targetSdk = 35
        versionCode = 6
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        manifestPlaceholders["NCP_KEY_ID"] =
            localProperties.getProperty("NAVER_MAP_CLIENT_ID")
                ?: localProperties.getProperty("naver.map.client.id")
                ?: ""

        buildConfigField("String", "API_BASE_URL", "\"${localProperties.buildConfigString("API_BASE_URL", "api.base.url", "https://api.example.com/")}\"")
        buildConfigField("String", "AI_API_BASE_URL", "\"${localProperties.buildConfigString("AI_API_BASE_URL", "ai.api.base.url", "https://fastapi.issueissyu-ai.cloud/")}\"")
        buildConfigField("String", "NAVER_CLIENT_ID", "\"${localProperties.buildConfigString("NAVER_CLIENT_ID", "naver.client.id", "")}\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${localProperties.buildConfigString("NAVER_CLIENT_SECRET", "naver.client.secret", "")}\"")
        buildConfigField("String", "NAVER_CLIENT_NAME", "\"${localProperties.buildConfigString("NAVER_CLIENT_NAME", "naver.client.name", "이슈있슈")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.google.play.services.location)
    implementation(libs.naver.map.sdk)
    implementation(libs.naver.nid.oauth)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.google.material)

    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    implementation(libs.androidx.security.crypto)

    implementation(libs.play.billing.ktx)
}
