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
                implementation(project(":sdk-api"))
                implementation(project(":platform-implementations"))
                implementation(project(":semconv"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(project(":test-fakes"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
