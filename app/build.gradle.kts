plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "codex.calculator"
    compileSdk = 36

    defaultConfig {
        applicationId = "codex.calculator"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.14.514codex"
    }

    signingConfigs {
        create("release") {
            // 修复 java.util 引用问题：直接导入 Properties 类
            val properties = java.util.Properties()
            try {
                val localPropertiesFile = rootProject.file("local.properties")
                if (localPropertiesFile.exists()) {
                    properties.load(localPropertiesFile.inputStream())
                    storeFile = file(properties.getProperty("storeFile", ""))
                    storePassword = properties.getProperty("storePassword", "")
                    keyAlias = properties.getProperty("keyAlias", "")
                    keyPassword = properties.getProperty("keyPassword", "")
                }
            } catch (e: Exception) {
                println("签名配置读取失败（不影响编译）: ${e.message}")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // 修复 kotlinOptions 归属问题：放在 android 块内，且补全 Kotlin 插件依赖
    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.02.01")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}