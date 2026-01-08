plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.kotlinx.kover")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":opentelemetry-kotlin-api"))
                implementation(project(":opentelemetry-kotlin-exporters-protobuf"))
                implementation(project(":opentelemetry-kotlin-platform-implementations"))
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.encoding)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":opentelemetry-kotlin-test-fakes"))
                implementation(libs.kotlin.test.common)
                implementation(libs.kotlin.test.common.annotations)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }
        val jvmAndAndroidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}
