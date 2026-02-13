plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
}

kotlin {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project(":implementation"))
                implementation(project(":exporters-otlp"))
                implementation(project(":exporters-core"))
                implementation(project(":exporters-protobuf"))
                implementation(project(":test-fakes"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.mock)
                implementation(libs.ktor.client.encoding)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(project(":compat"))
            }
        }
    }
}
