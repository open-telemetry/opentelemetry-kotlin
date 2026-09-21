plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":sdk-api"))
                api(project(":platform-implementations"))
            }
        }
    }
}
