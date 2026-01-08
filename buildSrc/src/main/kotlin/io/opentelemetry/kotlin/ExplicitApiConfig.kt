package io.opentelemetry.kotlin

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.configureExplicitApiMode(kotlin: KotlinMultiplatformExtension) {
    if (project.containsPublicApi()) {
        kotlin.compilerOptions {
            freeCompilerArgs.set(freeCompilerArgs.get().plus("-Xexplicit-api=strict"))
        }
    }
}
