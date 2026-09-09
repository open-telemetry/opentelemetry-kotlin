import com.squareup.wire.gradle.WireTask
import de.undercouch.gradle.tasks.download.Download

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.download)
    alias(libs.plugins.wire)
}

// The release version of https://github.com/open-telemetry/opentelemetry-proto used to generate
// classes from protobuf definitions
val otelProtoVersion = "1.8.0"
val otelProtoRepoZip =
    "https://github.com/open-telemetry/opentelemetry-proto/archive/v${otelProtoVersion}.zip"
val protoResultDir = layout.buildDirectory.dir("proto")

val downloadOtelProtoDefinitions = tasks.register<Download>("downloadOtelProtoDefinitions") {
    src(otelProtoRepoZip)
    dest(layout.buildDirectory.file("opentelemetry-proto-${otelProtoVersion}.zip"))
    overwrite(false)
}

val updateOtelProtoDefinitions = tasks.register<Copy>("updateOtelProtoDefinitions") {
    val prefix = "opentelemetry-proto-$otelProtoVersion"

    dependsOn(downloadOtelProtoDefinitions)
    from(zipTree(downloadOtelProtoDefinitions.get().dest)) {
        include("opentelemetry-proto-$otelProtoVersion/opentelemetry/proto/**")
        eachFile {
            path = path.removePrefix(prefix)
        }
        exclude {
            !it.path.endsWith(".proto")
        }
        includeEmptyDirs = false
    }
    into(protoResultDir)
}

tasks.withType<WireTask>().configureEach {
    dependsOn(updateOtelProtoDefinitions)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":sdk-api"))
                implementation(project(":sdk-common"))
                implementation(project(":api-ext"))
                implementation(libs.wire.runtime)
            }
        }
        commonTest {
            dependencies {
                implementation(project(":test-fakes"))
                implementation(libs.kotlin.test.common)
                implementation(libs.kotlin.test.common.annotations)
            }
        }
    }
}

// Disable Detekt tasks for generated code
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    enabled = false
}

wire {
    sourcePath {
        srcDir(protoResultDir)
    }
    kotlin {
        rpcRole = "none"
    }
}