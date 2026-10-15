package io.opentelemetry.kotlin

import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.Family

fun Project.configureLinuxTests(kotlin: KotlinMultiplatformExtension) {
    kotlin.targets.withType(KotlinNativeTarget::class.java).configureEach {
        if (konanTarget.family != Family.LINUX) {
            return@configureEach
        }
        val targetName = name
        val capitalizedName = targetName.replaceFirstChar { it.uppercase() }
        val testDirectory = layout.buildDirectory.dir("bin/$targetName/debugTest")
        val copyResources = tasks.register("copy${capitalizedName}TestResources", Copy::class.java) {
            from(layout.projectDirectory.dir("src/commonTest/resources"))
            into(testDirectory.map { it.dir("resources") })
        }
        tasks.named("linkDebugTest$capitalizedName").configure {
            dependsOn(copyResources)
        }
        tasks.withType(KotlinNativeTest::class.java).configureEach {
            if (name == "${targetName}Test") {
                workingDir = testDirectory.get().asFile.absolutePath
            }
        }
    }
}
