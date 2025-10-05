plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.rcbs.authenticator"
    compileSdk = 34  // 降低编译SDK版本以提高兼容性

    defaultConfig {
        applicationId = "com.rcbs.authenticator"
        minSdk = 30  // Android 11 (API 30) for Wear OS 3+
        targetSdk = 34  // 降低到34以提高兼容性
        versionCode = 1
        versionName = "1.0"
        
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")  // 支持更多架构
        }
    }

    signingConfigs {
        create("release") {
            // 使用调试签名进行快速测试
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Wear OS 核心依赖 - 设为可选，兼容没有Google服务的设备
    compileOnly("com.google.android.gms:play-services-wearable:18.1.0")
    
    // Compose BOM - 使用稳定版本以确保兼容性
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    
    // Wear Compose 专用库
    implementation("androidx.wear.compose:compose-material:1.3.1")
    implementation("androidx.wear.compose:compose-foundation:1.3.1")
    implementation("androidx.wear.compose:compose-navigation:1.3.1")
    
    // Activity Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Core 和 Lifecycle
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // Crypto for OTP generation
    implementation("commons-codec:commons-codec:1.15")
    
    // JSON serialization for backup/restore
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // 测试依赖
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}