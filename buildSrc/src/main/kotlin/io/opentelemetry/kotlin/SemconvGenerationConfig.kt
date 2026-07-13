package io.opentelemetry.kotlin

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import org.gradle.process.ExecOperations

/**
 * Registers the tasks that generate Kotlin sources from an OTel semantic convention registry.
 */
fun Project.registerSemanticConventionGeneration(
    registryName: String,
    version: String,
    repoZipUrl: String,
    packageName: String,
) {
    val registryDir = "semantic-conventions-$registryName-$version"

    val downloadSemanticConventions = tasks.register("downloadSemanticConventions", Download::class.java) {
        src(repoZipUrl)
        dest(layout.buildDirectory.file("$registryDir/semantic-conventions.zip"))
        overwrite(false)
    }

    val refreshSemanticConventions = tasks.register("refreshSemanticConventions", Copy::class.java) {
        dependsOn(downloadSemanticConventions)
        from(zipTree(downloadSemanticConventions.get().dest))
        eachFile {
            // Remove the top level folder
            val pathParts = path.split("/")
            path = pathParts.subList(1, pathParts.size).joinToString("/")
        }
        into(layout.buildDirectory.dir(registryDir))
    }

    tasks.register("generateSemanticConventions", GenerateSemanticConventionsTask::class.java) {
        modelDir.set("build/$registryDir/model")
        this.packageName.set(packageName)
        templatesDir.set(rootProject.layout.projectDirectory.dir("semconv/templates").asFile.absolutePath)
        dependsOn(refreshSemanticConventions)
    }
}

abstract class GenerateSemanticConventionsTask @Inject constructor(
    objectFactory: ObjectFactory,
    private val execOps: ExecOperations
) : DefaultTask() {

    @Input
    val modelDir: Property<String> = objectFactory.property(String::class.java)

    @Input
    val packageName: Property<String> = objectFactory.property(String::class.java)

    @Input
    val templatesDir: Property<String> = objectFactory.property(String::class.java)

    @TaskAction
    fun run() {
        try {
            execOps.exec {
                commandLine(
                    "weaver", "registry", "generate",
                    "-r", modelDir.get(), // directory for semantic convention schemas
                    "--templates", templatesDir.get(), // directory containing the shared jinja2 templates
                    "--param", "package=${packageName.get()}", // package of the generated classes
                    "kotlin", // use the Kotlin jinja2 template
                    "src/commonMain/kotlin/${packageName.get().replace('.', '/')}" // output directory
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
