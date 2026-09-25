plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("com.vanniktech.maven.publish")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":api-ext"))
            implementation(project(":semconv"))
            implementation(libs.kotlin.serialization)
        }
        commonTest.dependencies {
            implementation(project(":test-fakes"))
        }
    }
}
