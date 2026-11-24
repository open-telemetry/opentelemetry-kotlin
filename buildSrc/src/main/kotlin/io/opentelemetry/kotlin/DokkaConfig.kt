package io.opentelemetry.kotlin

import org.gradle.api.Project

/** Configures API documentation for published modules that expose public API. */
fun Project.configureDokka() {
    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        if (containsPublicApi()) {
            pluginManager.apply("org.jetbrains.dokka")
        }
    }
}
