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
                implementation(libs.kotlinx.coroutines)
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(project(":test-fakes"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
