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
                api(project(":api"))
                api(project(":sdk-api"))
                implementation(project(":sdk-common"))
                implementation(project(":platform-implementations"))
            }
        }
        commonTest {
            dependencies {
                implementation(project(":test-fakes"))
            }
        }
    }
}
