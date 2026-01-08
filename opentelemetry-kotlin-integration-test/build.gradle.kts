plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":opentelemetry-kotlin-api"))
                implementation(project(":opentelemetry-kotlin-test-fakes"))
                implementation(project(":opentelemetry-kotlin-semconv"))
                implementation(libs.kotlin.serialization)
            }
        }
    }
}
