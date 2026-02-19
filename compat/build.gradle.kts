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
                api(project(":core"))
                implementation(project(":sdk-api"))
                implementation(project(":model"))
                implementation(project(":java-typealiases"))
                implementation(project.dependencies.platform(libs.opentelemetry.bom))
                implementation(libs.opentelemetry.api)
                implementation(libs.opentelemetry.sdk)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(project(":test-fakes"))
                implementation(project(":integration-test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
