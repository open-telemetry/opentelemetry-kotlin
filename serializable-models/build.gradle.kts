plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":api-ext"))
            implementation(project(":test-fakes"))
            implementation(project(":semconv"))
            implementation(libs.kotlin.serialization)
        }
    }
}
