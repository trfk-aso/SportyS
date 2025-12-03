import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
    kotlin("plugin.serialization") version "1.9.10"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
            implementation(libs.koin.android.compat)
            implementation(libs.koin.androidx.compose)
            implementation("com.android.billingclient:billing-ktx:6.0.1")
            implementation("io.ktor:ktor-client-okhttp:2.3.7")
        }
        commonMain.dependencies {
            implementation("media.kamel:kamel-image:0.9.5")
            implementation("io.ktor:ktor-client-core:2.3.7")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Database
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)
// Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
// DateTime
            implementation(libs.kotlinx.datetime)
// Navigation
            implementation(libs.navigation.compose)
// Settings
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.koin.compose.viewmodel)
            implementation(compose.materialIconsExtended)
            api(libs.kmpnotifier)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            implementation(libs.native.driver)
            implementation("io.ktor:ktor-client-darwin:2.3.7")
        }
    }
}

android {
    namespace = "com.example.sportys"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.sportys"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

sqldelight {
    databases {
        create("SportyS") {
            packageName.set("com.example.sportys.data")
            srcDirs("src/commonMain/kotlin")
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

