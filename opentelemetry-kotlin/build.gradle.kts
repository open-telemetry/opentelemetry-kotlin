plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":opentelemetry-kotlin-api"))
                api(project(":opentelemetry-kotlin-api-ext"))
                api(project(":opentelemetry-kotlin-noop"))
                api(project(":opentelemetry-kotlin-exporters-core"))
                implementation(project(":opentelemetry-kotlin-model"))
            }
        }
    }
}
