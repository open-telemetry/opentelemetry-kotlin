package io.opentelemetry.kotlin

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

private const val EXPERIMENTAL_API_ANNOTATION = "io.opentelemetry.kotlin.ExperimentalApi"

fun Project.configureOptIn(kotlin: KotlinMultiplatformExtension) {
    if (isOptInEnabled()) {
        kotlin.compilerOptions {
            optIn.add(EXPERIMENTAL_API_ANNOTATION)
        }
    }
}

fun Project.isOptInEnabled(): Boolean {
    return findProperty("io.opentelemetry.kotlin.enableOptIn")?.toString()?.toBoolean() ?: true
}
