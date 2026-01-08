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
                api(project(":opentelemetry-kotlin-api"))
                api(project(":opentelemetry-kotlin-api-ext"))
                api(project(":opentelemetry-kotlin-noop"))
                implementation(project(":opentelemetry-kotlin-model"))
                implementation(project(":opentelemetry-kotlin-implementation"))
            }
        }
        val jvmMain by getting {
            dependencies {
                api(project(":opentelemetry-kotlin-exporters-core"))
                api(project(":opentelemetry-kotlin-exporters-otlp"))
            }
        }
    }
}
