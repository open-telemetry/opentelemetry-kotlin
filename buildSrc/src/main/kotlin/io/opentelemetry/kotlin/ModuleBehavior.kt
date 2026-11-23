package io.opentelemetry.kotlin

import org.gradle.api.Project

/**
 * Whether a module is a public entrypoint whose API consumers compile against. Modules opt in by setting
 * `io.opentelemetry.kotlin.containsPublicApi=true` in their `gradle.properties`. This enables explicit API mode and
 * binary compatibility validation. Defaults to false.
 */
fun Project.containsPublicApi(): Boolean {
    return findProperty("io.opentelemetry.kotlin.containsPublicApi")?.toString()
        ?.toBoolean() ?: false
}

/**
 * Whether a module consists of a Java + Android implementation only.
 * the presence of the property `io.opentelemetry.kotlin.jvmAndroidModule` in the module.
 * We only need to build JVM/Android targets for these modules.
 */
fun Project.isJvmAndroidModule(): Boolean {
    return findProperty("io.opentelemetry.kotlin.jvmAndroidModule")?.toString()?.toBoolean()
        ?: false
}
