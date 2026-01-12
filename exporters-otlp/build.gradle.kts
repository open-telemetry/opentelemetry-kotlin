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
                implementation(project(":api"))
                implementation(project(":exporters-protobuf"))
                implementation(project(":platform-implementations"))
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.encoding)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-fakes"))
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
