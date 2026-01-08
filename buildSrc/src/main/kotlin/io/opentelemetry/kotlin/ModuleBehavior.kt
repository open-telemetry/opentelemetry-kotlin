package io.opentelemetry.kotlin

import org.gradle.api.Project

/**
 * Whether a module contains interfaces that form part of the public API. This is determined by the presence
 * of the property `io.opentelemetry.kotlin.containsPublicApi` in the module. This enables explicit API mode and binary
 * compatibility validation.
 */
fun Project.containsPublicApi(): Boolean {
    return findProperty("io.opentelemetry.kotlin.containsPublicApi")?.toString()
        ?.toBoolean() ?: true
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
