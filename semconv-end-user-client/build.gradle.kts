import io.opentelemetry.kotlin.registerSemanticConventionGeneration

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
}

val semanticConventionsVersion = libs.versions.semanticConventionsEndUserClient.get()

registerSemanticConventionGeneration(
    registryName = "end-user-client",
    version = semanticConventionsVersion,
    repoZipUrl = "https://github.com/bidetofevil/semantic-conventions-end-user-client/archive/v${semanticConventionsVersion}.zip",
    packageName = "io.opentelemetry.kotlin.semconv.enduserclient",
)

// Disable Detekt tasks for generated code
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    enabled = false
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":semconv"))
            }
        }
    }
}
