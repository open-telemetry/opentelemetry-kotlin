package io.opentelemetry.kotlin

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

// workaround: see https://medium.com/@saulmm2/android-gradle-precompiled-scripts-tomls-kotlin-dsl-df3c27ea017c
fun Project.findLibrary(alias: String) =
    project.extensions.getByType<VersionCatalogsExtension>().named("libs").findLibrary(alias).get()
