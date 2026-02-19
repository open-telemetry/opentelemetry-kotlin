plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.benchmark.runtime)
                implementation(project(":sdk-api"))
                implementation(project(":api-ext"))
                implementation(project(":noop"))
                implementation(project(":implementation"))
            }
        }
        val jvmAndAndroidMain by getting {
            dependencies {
                implementation(project.dependencies.platform(libs.opentelemetry.bom))
                implementation(libs.opentelemetry.api)
                implementation(libs.opentelemetry.sdk)
                implementation(project(":sdk-api"))
                implementation(project(":api-ext"))
                implementation(project(":noop"))
                implementation(project(":compat"))
                implementation(project(":implementation"))
                implementation(project(":java-typealiases"))
            }
        }
    }
}
