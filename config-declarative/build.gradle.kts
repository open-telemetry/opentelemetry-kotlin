plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":sdk-api"))
                implementation(project(":config"))
                implementation(project(":config-yaml"))
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(project(":implementation"))
                implementation(project(":compat"))
                implementation(project(":test-fakes"))
                implementation(libs.kotlinx.coroutines)
            }
        }
    }
}
