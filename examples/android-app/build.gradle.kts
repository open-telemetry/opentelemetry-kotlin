import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
}

dependencies {
    implementation(project(":examples:example-common"))
}

android {
    namespace = "io.opentelemetry.kotlin.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.opentelemetry.kotlin.example"
        minSdk = 21
        targetSdk = 36
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}
