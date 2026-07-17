package io.opentelemetry.kotlin.config.schema

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Generates the declarative-configuration Kotlin data model from the downloaded
 * `opentelemetry-configuration` JSON schema.
 */
abstract class GenerateOpenTelemetryConfigurationTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val schemaFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        @Suppress("UNCHECKED_CAST")
        val schema = JsonSlurper().parse(schemaFile.get().asFile) as Map<String, Any?>

        val destination = outputDir.get().asFile
        destination.deleteRecursively()
        destination.mkdirs()

        val files = ConfigModelGenerator(PACKAGE).generate(schema)
        files.forEach { file ->
            File(destination, "${file.name}.kt").writeText(file.toString())
        }
        logger.lifecycle("Generated ${files.size} declarative-configuration types into $destination")
    }

    private companion object {
        const val PACKAGE = "io.opentelemetry.kotlin.config.schema.model"
    }
}
