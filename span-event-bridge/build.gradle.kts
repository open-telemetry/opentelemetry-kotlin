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
                implementation(project(":sdk-api"))
                implementation(project(":sdk-common"))
                implementation(project(":api-ext"))
            }
        }
        commonTest {
            dependencies {
                implementation(project(":test-fakes"))
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
