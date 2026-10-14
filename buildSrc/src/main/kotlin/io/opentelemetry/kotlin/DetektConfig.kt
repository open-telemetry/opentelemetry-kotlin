package io.opentelemetry.kotlin

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

fun Project.configureDetekt() {
    project.pluginManager.apply("io.gitlab.arturbosch.detekt")

    project.dependencies.add(
        "detektPlugins",
        project.findLibrary("detekt-formatting")
    )
    val detekt = project.extensions.getByType(DetektExtension::class.java)

    detekt.apply {
        buildUponDefaultConfig = true
        autoCorrect = true
        parallel = true
        config.from(project.files("${project.rootDir}/config/detekt/detekt.yml")) // overwrite default behaviour here
        baseline =
            project.file("${project.projectDir}/config/detekt/baseline.xml") // suppress pre-existing issues
    }

    // setup custom task to run on commonTest
    // see https://detekt.dev/docs/gettingstarted/type-resolution/#enabling-on-a-kmp-project
    tasks.register<Detekt>("detektCommonTest") {
        source(files("src/commonTest/kotlin"))
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        autoCorrect = true
        buildUponDefaultConfig = true
    }
    project.tasks.withType(Detekt::class.java).configureEach {
        jvmTarget = "11"
        reports {
            // set explicit report name to avoid tasks invalidating each others outputs
            html.outputLocation.set(layout.buildDirectory.file("reports/detekt/$name.html"))
            xml.required.set(false)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }
    project.tasks.withType(DetektCreateBaselineTask::class.java).configureEach {
        jvmTarget = "11"
    }

    project.tasks.named("check").configure {
        // detektMetadataMain analyses the same sources as detektMetadataCommonMain
        dependsOn(project.tasks.withType(Detekt::class.java).matching { it.name != "detektMetadataMain" })
    }
}
