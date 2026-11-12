import io.opentelemetry.kotlin.CheckDuplicatePublicApiTask
import io.opentelemetry.kotlin.docs.DocSnippetsTask
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    base
    id("com.android.kotlin.multiplatform.library") apply false
    id("org.jetbrains.kotlin.multiplatform") apply false
    id("com.vanniktech.maven.publish") apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.11" apply false
}

plugins.withType<YarnPlugin> {
    extensions.configure<YarnRootExtension> {
        resolution("brace-expansion", ">=5.0.12")
        resolution("diff", ">=8.0.3")
        resolution("glob", ">=14.0.2")
        resolution("js-yaml", ">=5.4.1")
        resolution("minimatch", ">=11.0.1")
        resolution("serialize-javascript", ">=7.1.4")
        resolution("**/ws", "8.21.0")
        resolution("ws", "8.21.0")
    }
}

group = "io.opentelemetry.kotlin"
version = project.version

val publishingToMavenLocal = gradle.startParameter.taskNames.any {
    it.substringAfterLast(':') == "publishToMavenLocal"
}
val snapshotPublish = project.findProperty("snapshotPublish")
    ?.toString()
    ?.toBoolean()
    ?: publishingToMavenLocal

if (snapshotPublish) {
    allprojects {
        version = "${version}-SNAPSHOT"
    }
}

val checkDuplicatePublicApi = tasks.register<CheckDuplicatePublicApiTask>("checkDuplicatePublicApi") {
    description = "Fails if two modules declare the same public class or top-level function."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    apiDumps.from(
        fileTree(rootDir) {
            include("**/api/jvm/*.api")
            exclude("**/build/**", "buildSrc/**")
        }
    )
}

fun DocSnippetsTask.configureDocSnippets(checkOnly: Boolean) {
    group = "documentation"
    this.checkOnly.set(checkOnly)
    rootDirectory.set(layout.projectDirectory)
    snippetSources.from(fileTree("docs-snippets/src") { include("**/*.kt") })
    markdownFiles.from(
        fileTree(rootDir) {
            include("docs/**/*.md", "**/README.md")
            exclude("**/build/**", "**/node_modules/**", ".*/**", "kotlin-js-store/**")
        }
    )
    tokens.put("otelKotlinVersion", providers.gradleProperty("io.opentelemetry.kotlin.latestReleaseVersion"))
}

tasks.register<DocSnippetsTask>("syncDocSnippets") {
    description = "Copies the code regions in docs-snippets into the markdown docs that reference them."
    configureDocSnippets(checkOnly = false)
    doNotTrackState("Rewrites its markdown inputs in place")
}

val checkDocSnippets = tasks.register<DocSnippetsTask>("checkDocSnippets") {
    description = "Fails if the markdown docs don't match the code regions in docs-snippets."
    configureDocSnippets(checkOnly = true)
}

tasks.named("check") {
    dependsOn(checkDuplicatePublicApi, checkDocSnippets)
}

tasks.register("verify") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs static analysis and tests, excluding the Gradle integration tests."
    val gradleIntegrationTest = ":gradle-integration-test"
    dependsOn(checkDuplicatePublicApi, checkDocSnippets)
    subprojects.filter { it.path != gradleIntegrationTest }.forEach { project ->
        project.plugins.withType(LifecycleBasePlugin::class.java) {
            dependsOn(project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME))
        }
    }
}

tasks.register("ciBuild") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs everything the gradle-test CI job checks. CI adds -x :gradle-integration-test:test."
    val perProjectTasks = setOf(
        LifecycleBasePlugin.BUILD_TASK_NAME,
        "linuxX64Test",
        "linkDebugTestLinuxArm64",
        "koverXmlReport",
    )
    allprojects.forEach { project ->
        dependsOn(provider { project.tasks.names.filter { it in perProjectTasks }.map { "${project.path}:$it" } })
    }
    dependsOn(
        ":benchmark-android:assemble",
        ":benchmark-android:compileReleaseAndroidTestKotlin",
        ":benchmark-jvm:assemble",
        ":examples:example-app-android:assembleRelease",
    )
}

// Kover instruments every JVM test class, so only apply it on CI (where coverage is reported) or
// when requested with -Pio.opentelemetry.kotlin.coverage=true.
val koverEnabled = providers.environmentVariable("CI").isPresent ||
    providers.gradleProperty("io.opentelemetry.kotlin.coverage").map { it.toBoolean() }.getOrElse(false)

if (koverEnabled) {
    apply(plugin = "org.jetbrains.kotlinx.kover")
    configureKover()
}

fun configureKover() = extensions.configure<KoverProjectExtension> {
    merge {
        subprojects { project ->
            project.findProperty("io.opentelemetry.kotlin.enableCodeCoverage")?.toString()?.toBoolean() ?: true
        }
    }
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
                // generated code: protobuf messages and the opentelemetry-configuration schema model
                classes(
                    "*.BuildConfig",
                    "io.opentelemetry.kotlin.proto.*",
                    "io.opentelemetry.kotlin.config.schema.model.*",
                )
            }
        }
    }
}
