import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.androidx.benchmark)
}

android {
    namespace = "io.opentelemetry.kotlin.benchmark"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
    }

    testBuildType = "release"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    buildTypes {
        release {
            isDefault = true
            isMinifyEnabled = false
        }
    }
}

dependencies {
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.benchmark.junit4)
    androidTestImplementation(project.dependencies.platform(libs.opentelemetry.bom))
    androidTestImplementation(libs.opentelemetry.api)
    androidTestImplementation(libs.opentelemetry.sdk)
    androidTestImplementation(project(":opentelemetry-kotlin"))
    androidTestImplementation(project(":opentelemetry-kotlin-compat"))
    androidTestImplementation(project(":opentelemetry-kotlin-implementation"))
    androidTestImplementation(project(":opentelemetry-kotlin-benchmark-fixtures"))
    androidTestImplementation(project(":opentelemetry-java-typealiases"))
}
