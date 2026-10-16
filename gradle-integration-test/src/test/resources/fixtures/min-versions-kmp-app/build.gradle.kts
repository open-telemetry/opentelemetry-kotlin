import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    alias(libs.plugins.test.kotlin.multiplatform.klib)
}

val otelKotlinVersion: String = providers.gradleProperty("otelKotlinVersion").get()

val otelModules: List<String> = providers.gradleProperty("klibModules").get()
    .split(",")
    .map(String::trim)
    .filter(String::isNotEmpty)

kotlin {
    js {
        nodejs()
        binaries.library()
    }
    linuxX64 {
        binaries.executable(listOf(NativeBuildType.DEBUG)) {
            entryPoint = "io.opentelemetry.kotlin.minversion.main"
        }
    }
    linuxArm64 {
        binaries.executable(listOf(NativeBuildType.DEBUG)) {
            entryPoint = "io.opentelemetry.kotlin.minversion.main"
        }
    }
    iosArm64()
    iosSimulatorArm64()
    tvosArm64()
    tvosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                otelModules.forEach { module ->
                    implementation("io.opentelemetry.kotlin:$module:$otelKotlinVersion")
                }
            }
        }
    }
}
