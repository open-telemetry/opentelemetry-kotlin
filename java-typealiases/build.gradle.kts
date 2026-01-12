plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
}

kotlin {
    sourceSets {
        val jvmAndAndroidMain by getting {
            dependencies {
                implementation(project.dependencies.platform(libs.opentelemetry.bom))
                implementation(libs.opentelemetry.api)
                implementation(libs.opentelemetry.sdk)
            }
        }
    }
}
