package io.opentelemetry.kotlin.integration.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class MinSupportedVersionsTest {

    @Test
    fun `min supported versions can consume artifacts`(@TempDir tmp: Path) {
        val fixtureSrc = File(systemProperty("fixtureSrcDir"), "min-versions-android-app").toPath()
        copyRecursively(fixtureSrc, tmp)

        val runner = GradleRunner.create()
            .withProjectDir(tmp.toFile())
            .withGradleVersion(systemProperty("minSupportedGradle"))
            .withArguments(
                "-PcatalogPath=${systemProperty("catalogPath")}",
                "-PotelKotlinVersion=${systemProperty("otelKotlinVersion")}",
                "-Pandroid.useAndroidX=true",
                "-Pandroid.nonTransitiveRClass=true",
                "assemble",
                "--info",
                "--stacktrace",
                "--no-configuration-cache",
            )
            .forwardOutput()
        runner.build()
    }

    private fun systemProperty(name: String): String =
        requireNotNull(System.getProperty(name)) { "system property '$name' was not set by the Gradle test task" }

    private fun copyRecursively(source: Path, target: Path) {
        Files.walk(source).use { stream ->
            stream.forEach { path ->
                val rel = source.relativize(path)
                val dest = target.resolve(rel.toString())
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest)
                } else {
                    Files.createDirectories(dest.parent)
                    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }
}
