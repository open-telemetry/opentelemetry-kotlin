package io.opentelemetry.kotlin

import org.gradle.api.Project
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

fun Project.configureTest() {
    tasks.withType(AbstractTestTask::class.java).configureEach {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}
