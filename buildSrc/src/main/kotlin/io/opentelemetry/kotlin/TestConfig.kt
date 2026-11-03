package io.opentelemetry.kotlin

import org.gradle.api.Project
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

fun Project.configureTest() {
    tasks.withType(AbstractTestTask::class.java).configureEach {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    // JVM test tasks only: JS and native test tasks are not of type Test.
    tasks.withType(Test::class.java).configureEach {
        // Test JVMs are short-lived, so C2 compilation costs more than it saves.
        jvmArgs("-XX:TieredStopAtLevel=1")
    }
}
