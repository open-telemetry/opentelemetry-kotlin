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
                api(project(":behavior"))
                api(project(":sdk-api"))
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
