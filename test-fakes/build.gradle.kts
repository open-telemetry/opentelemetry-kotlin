plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":sdk"))
                api(project(":platform-implementations"))
            }
        }
    }
}
