import de.undercouch.gradle.tasks.download.Download

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.opentelemetry.kotlin.build-logic")
    id("signing")
    id("com.vanniktech.maven.publish")
    alias(libs.plugins.download)
}

// The release version of https://github.com/open-telemetry/semantic-conventions used to generate classes
val semanticConventionsVersion = "1.37.0"
val semanticConventionsRepoZip =
    "https://github.com/open-telemetry/semantic-conventions/archive/v${semanticConventionsVersion}.zip"

// Disable Detekt tasks for generated code
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    enabled = false
}

tasks.register<GenerateSemanticConventionsTask>("generateSemanticConventions").configure {
    version.set(semanticConventionsVersion)
    dependsOn(refreshSemanticConventions)
}

abstract class GenerateSemanticConventionsTask @Inject constructor(
    objectFactory: ObjectFactory,
    private val execOps: ExecOperations
) : DefaultTask() {

    @Input
    val version: Property<String> = objectFactory.property<String>()

    @TaskAction
    fun run() {
        try {
            execOps.exec {
                commandLine(
                    "weaver", "registry", "generate",
                    "-r", "build/semantic-conventions-${version.get()}/model", // directory for semantic convention schemas
                    "--templates", "templates", // directory containing jinja2 templates
                    "kotlin", // use the Kotlin jinja2 template
                    "src/commonMain/kotlin/io/opentelemetry/kotlin/semconv" // output directory
                )
                isIgnoreExitValue = true
            }
        } catch (exc: Exception) {
            throw GradleException(
                "OTel weaver command failed. Is it installed and on the path?",
                exc
            )
        }
    }
}

val downloadSemanticConventions by tasks.registering(Download::class) {
    src(semanticConventionsRepoZip)
    dest(layout.buildDirectory.file("semantic-conventions-${semanticConventionsVersion}/semantic-conventions.zip"))
    overwrite(false)
}

val refreshSemanticConventions by tasks.registering(Copy::class) {
    dependsOn(downloadSemanticConventions)

    from(zipTree(downloadSemanticConventions.get().dest))
    eachFile(closureOf<FileCopyDetails> {
        // Remove the top level folder
        val pathParts = path.split("/")
        path = pathParts.subList(1, pathParts.size).joinToString("/")
    })
    into(layout.buildDirectory.dir("semantic-conventions-${semanticConventionsVersion}/"))
}
