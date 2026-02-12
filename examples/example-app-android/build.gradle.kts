import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "io.opentelemetry.kotlin.example.app.android"
    compileSdk = 36
    defaultConfig {
        applicationId = "io.opentelemetry.kotlin.example.app"
        minSdk = 23
        targetSdk = 36
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { compose = true }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
}

dependencies {
    implementation(project(":examples:example-app"))
    implementation(libs.androidx.activity.compose)
}
