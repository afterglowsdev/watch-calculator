plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose") // Compose 插件保留（和 Android 插件不冲突）
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

    // Kotlin DSL 正确的签名配置
    signingConfigs {
        create("release") {
            val properties = java.util.Properties()
            try {
                properties.load(rootProject.file("local.properties").inputStream())
                // 读取签名配置，空值兜底避免编译失败
                storeFile = file(properties.getProperty("storeFile") ?: "")
                storePassword = properties.getProperty("storePassword")
                keyAlias = properties.getProperty("keyAlias")
                keyPassword = properties.getProperty("keyPassword")
            } catch (e: Exception) {
                println("签名配置读取失败（非关键错误）: ${e.message}")
            }
        }
    }

    buildTypes {
        release {
            // 关联签名配置
            signingConfig = signingConfigs.getByName("release")
            // Kotlin DSL 正确写法（isMinifyEnabled 替代 minifyEnabled）
            isMinifyEnabled = false
            // 括号+双引号，适配 Kotlin 语法
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

    kotlinOptions {
        jvmTarget = "17"
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