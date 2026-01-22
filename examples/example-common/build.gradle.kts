import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {}
    js(IR) {
        nodejs()
        browser()
        binaries.library()
    }
    val frameworkName = "OtelKotlinExample"
    val framework = XCFramework(frameworkName)

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = frameworkName
        }
        framework.add(target.binaries.getFramework("RELEASE"))
    }
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":api"))
                api(project(":api-ext"))
                api(project(":noop"))
                implementation(project(":model"))
                implementation(project(":implementation"))
                implementation(libs.kotlinx.coroutines)
            }
        }
        val jvmMain by getting {
            dependencies {
                api(project(":exporters-core"))
                api(project(":exporters-otlp"))
            }
        }
    }
}
