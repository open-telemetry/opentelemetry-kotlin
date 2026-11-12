package io.opentelemetry.kotlin.docs

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Copies the snippets in [snippetSources] into [markdownFiles]. See [DocSnippets] for the format.
 */
abstract class DocSnippetsTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val snippetSources: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val markdownFiles: ConfigurableFileCollection

    /**
     * Values substituted for `{{name}}` tokens in snippet text.
     */
    @get:Input
    abstract val tokens: MapProperty<String, String>

    @get:Input
    abstract val checkOnly: Property<Boolean>

    /**
     * Directory that file names in messages are relative to.
     */
    @get:Internal
    abstract val rootDirectory: DirectoryProperty

    @TaskAction
    fun run() {
        val root = rootDirectory.get().asFile
        val errors = mutableListOf<String>()
        val snippets = DocSnippets.parseSnippets(
            snippetSources.files.associate { it.relativeTo(root).path to it.readText() },
            errors,
        )
        val changed = markdownFiles.files.sorted().mapNotNull { file ->
            val original = file.readText()
            val rendered = DocSnippets.render(file.relativeTo(root).path, original, snippets, tokens.get(), errors)
            (file to rendered).takeIf { rendered != original }
        }

        if (errors.isNotEmpty()) {
            throw GradleException(errors.joinToString("\n", prefix = "Invalid doc snippets:\n") { "  $it" })
        }
        if (checkOnly.get() && changed.isNotEmpty()) {
            throw GradleException(
                changed.joinToString(
                    separator = "\n",
                    prefix = "Doc snippets are out of date in:\n",
                    postfix = "\nRun ./gradlew syncDocSnippets to update them.",
                ) { "  ${it.first.relativeTo(root).path}" }
            )
        }
        changed.forEach { (file, text) -> file.writeText(text) }
    }
}
