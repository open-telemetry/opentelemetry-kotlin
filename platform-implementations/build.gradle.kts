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
        commonMain {
            dependencies {
                implementation(project(":api"))
                implementation(project(":error-handling"))
                api(libs.kotlinx.coroutines)
                api(libs.okio)
            }
        }
        commonTest {
            dependencies {
            }
        }
    }
}
