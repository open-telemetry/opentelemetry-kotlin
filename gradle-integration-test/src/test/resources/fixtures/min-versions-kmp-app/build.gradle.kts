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
    iosArm64()
    iosSimulatorArm64()

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
