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
        val jvmAndAndroidMain by getting {
            dependencies {
                api(project(":api"))
                implementation(libs.kotlinx.coroutines)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(project(":test-fakes"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
