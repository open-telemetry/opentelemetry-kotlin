import de.undercouch.gradle.tasks.download.Download

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.download)
}

// release version of https://github.com/open-telemetry/opentelemetry-configuration
val openTelemetryConfigurationVersion = "1.1.0"
val openTelemetryConfigurationRepoZip =
    "https://github.com/open-telemetry/opentelemetry-configuration/archive/v${openTelemetryConfigurationVersion}.zip"

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":sdk-api"))
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

// This task will generate Kotlin data-model classes from the downloaded opentelemetry-configuration
// JSON schema. It's currently a no-op.
tasks.register("generateOpenTelemetryConfiguration") {
    dependsOn(refreshOpenTelemetryConfiguration)
    val schemaDir =
        layout.buildDirectory.dir("opentelemetry-configuration-${openTelemetryConfigurationVersion}")
    doLast {
        logger.lifecycle(
            "generateOpenTelemetryConfiguration is a no-op placeholder; " +
                    "schema available at ${schemaDir.get().asFile}"
        )
    }
}
