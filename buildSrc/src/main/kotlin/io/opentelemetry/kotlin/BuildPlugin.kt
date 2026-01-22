package io.opentelemetry.kotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class BuildPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
            project.configureKotlin(kotlin)
            project.configureDetekt()
            project.configureBinaryCompatValidation()
            project.configureExplicitApiMode(kotlin)
            project.configureTest()
        }
        project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            val kotlin = project.extensions.getByType(KotlinJvmProjectExtension::class.java)
            project.configureDetekt()
            project.configureBinaryCompatValidation()
            project.configureTest()
        }
        project.configurePublishing()
    }
}
