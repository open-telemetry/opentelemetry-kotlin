package io.opentelemetry.kotlin

import org.gradle.api.Project
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.time.Duration

/**
 * Upper bound on a single test task so that hangs fail faster than the default CI job timeout.
 */
val TEST_TASK_TIMEOUT: Duration = Duration.ofMinutes(10)

fun Project.configureTest() {
    tasks.withType(AbstractTestTask::class.java).configureEach {
        timeout.set(TEST_TASK_TIMEOUT)
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            warn {
                events(TestLogEvent.FAILED)
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }

    // JVM test tasks only: JS and native test tasks are not of type Test.
    tasks.withType(Test::class.java).configureEach {
        // Test JVMs are short-lived, so C2 compilation costs more than it saves.
        jvmArgs("-XX:TieredStopAtLevel=1")
    }
}
