import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import java.time.Duration

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
                api(project(":api"))
                api(project(":sdk-api"))
                implementation(project(":sdk-common"))
                implementation(project(":context-single-slot"))
                implementation(project(":propagation-utils"))
                implementation(project(":model"))
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

tasks.withType<KotlinNativeTest>().configureEach {
    timeout.set(Duration.ofMinutes(30))
}
