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
                api(project(":behavior"))
                api(project(":config-dsl"))
                api(project(":config-envar"))
                api(project(":sdk-api"))
                implementation(project(":config-yaml"))
                implementation(project(":platform-implementations"))
                implementation(libs.okio)
            }
        }
        commonTest {
            dependencies {
                implementation(project(":test-fakes"))
                implementation(libs.kotlin.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
