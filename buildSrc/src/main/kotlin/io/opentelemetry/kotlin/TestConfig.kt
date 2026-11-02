package io.opentelemetry.kotlin

import org.gradle.api.Project
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

fun Project.configureTest() {
    tasks.withType(AbstractTestTask::class.java).configureEach {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            warn {
                events(TestLogEvent.FAILED)
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }
}
