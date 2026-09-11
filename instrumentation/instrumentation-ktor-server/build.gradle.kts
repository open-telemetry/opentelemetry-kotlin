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
        getByName("jvmAndAndroidMain") {
            dependencies {
                api(project(":api"))
                api(libs.ktor.server.core)
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(project(":implementation"))
                implementation(project(":test-fakes"))
                implementation(libs.ktor.server.test.host)
            }
        }
    }
}
