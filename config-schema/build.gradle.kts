import de.undercouch.gradle.tasks.download.Download
import io.gitlab.arturbosch.detekt.Detekt
import io.opentelemetry.kotlin.config.schema.GenerateOpenTelemetryConfigurationTask

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.download)
    alias(libs.plugins.kotlin.serialization)
}

// release version of https://github.com/open-telemetry/opentelemetry-configuration
val openTelemetryConfigurationVersion = "1.1.0"
val openTelemetryConfigurationRepoZip =
    "https://github.com/open-telemetry/opentelemetry-configuration/archive/v${openTelemetryConfigurationVersion}.zip"

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.serialization)
            }
        }
    }
}

val downloadOpenTelemetryConfiguration by tasks.registering(Download::class) {
    src(openTelemetryConfigurationRepoZip)
    dest(
        layout.buildDirectory.file(
            "opentelemetry-configuration-${openTelemetryConfigurationVersion}/opentelemetry-configuration.zip"
        )
    )
    overwrite(false)
}

val refreshOpenTelemetryConfiguration by tasks.registering(Copy::class) {
    dependsOn(downloadOpenTelemetryConfiguration)

    from(zipTree(downloadOpenTelemetryConfiguration.get().dest))
    eachFile(closureOf<FileCopyDetails> {
        // remove top level folder (opentelemetry-configuration-<version>/)
        val pathParts = path.split("/")
        path = pathParts.subList(1, pathParts.size).joinToString("/")
    })
    into(layout.buildDirectory.dir("opentelemetry-configuration-${openTelemetryConfigurationVersion}/"))
}

// generate kotlin from the downloaded opentelemetry-configuration JSON schema
tasks.register<GenerateOpenTelemetryConfigurationTask>("generateOpenTelemetryConfiguration") {
    dependsOn(refreshOpenTelemetryConfiguration)
    schemaFile.set(
        layout.buildDirectory.file(
            "opentelemetry-configuration-${openTelemetryConfigurationVersion}/opentelemetry_configuration.json"
        )
    )
    outputDir.set(
        layout.projectDirectory.dir("src/commonMain/kotlin/io/opentelemetry/kotlin/config/schema/model")
    )
}

// exclude generated code from static analysis
tasks.withType<Detekt>().configureEach {
    exclude("**/config/schema/model/**")
}
