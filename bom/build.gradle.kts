plugins {
    id("java-platform")
    id("signing")
    id("com.vanniktech.maven.publish")
    id("io.opentelemetry.kotlin.build-logic")
}

description = "OpenTelemetry Kotlin BOM"

// projects that must not be evaluated from here: this project, and those that themselves
// call evaluationDependsOn() on every subproject (which would otherwise be circular)
val excludedProjects = setOf(path, ":gradle-integration-test")

// constrain every published opentelemetry-kotlin library so that versions stay in lockstep.
// A BOM only adds version constraints: it never adds a dependency on any of these modules.
val publishedModules = rootProject.subprojects
    .filter { it.path !in excludedProjects }
    .map { evaluationDependsOn(it.path) }
    .filter { it.plugins.hasPlugin("com.vanniktech.maven.publish") }
    .filter { it.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") } // avoids BOM modules
    .sortedBy { it.path }

dependencies {
    constraints {
        publishedModules.forEach { api(it) }
    }
}
