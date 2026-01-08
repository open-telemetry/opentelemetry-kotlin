package io.opentelemetry.kotlin

import kotlinx.validation.ApiValidationExtension
import org.gradle.api.Project

fun Project.configureBinaryCompatValidation() {
    project.pluginManager.apply("binary-compatibility-validator")
    val apiValidation = project.extensions.getByType(ApiValidationExtension::class.java)
    apiValidation.validationDisabled = !project.containsPublicApi()
}
