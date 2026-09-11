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
        getByName("commonMain") {
            dependencies {
                implementation(project(":sdk-api"))
                implementation(project(":platform-implementations"))
                implementation(project(":semconv"))
                implementation(libs.kotlinx.coroutines)
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(project(":test-fakes"))
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
