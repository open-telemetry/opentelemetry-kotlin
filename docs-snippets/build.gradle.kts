plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":implementation"))
                implementation(project(":exporters-otlp"))
                implementation(project(":config-declarative"))
                implementation(project(":context-threadlocal"))
            }
        }
    }
}
