plugins {
    alias(libs.plugins.test.kotlin.multiplatform.klib)
}

val otelKotlinVersion: String = providers.gradleProperty("otelKotlinVersion").get()

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
                implementation("io.opentelemetry.kotlin:api:$otelKotlinVersion")
                implementation("io.opentelemetry.kotlin:sdk-api:$otelKotlinVersion")
                implementation("io.opentelemetry.kotlin:core:$otelKotlinVersion")
                implementation("io.opentelemetry.kotlin:implementation:$otelKotlinVersion")
                implementation("io.opentelemetry.kotlin:exporters-core:$otelKotlinVersion")
            }
        }
    }
}
