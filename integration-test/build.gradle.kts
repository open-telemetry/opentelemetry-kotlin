plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(project(":api-ext"))
                implementation(project(":sdk-api"))
                implementation(project(":test-fakes"))
                implementation(project(":semconv"))
                implementation(project(":exporters-core"))
                implementation(libs.kotlin.serialization)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
