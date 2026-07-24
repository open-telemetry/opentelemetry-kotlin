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
                implementation(project(":api-ext"))
                implementation(project(":sdk-api"))
                implementation(project(":sdk-common"))
                implementation(project(":semconv"))
                implementation(project(":exporters-core"))
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.okio)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-fakes"))
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
