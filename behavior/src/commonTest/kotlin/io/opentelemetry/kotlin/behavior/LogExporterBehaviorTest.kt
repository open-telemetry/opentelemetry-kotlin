package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertSame

internal class LogExporterBehaviorTest {

    @Test
    fun consoleIsSingleton() {
        assertSame(LogExporterBehavior.Console, LogExporterBehavior.Console)
    }

    @Test
    fun consoleMergesToHigher() {
        assertSame(LogExporterBehavior.Console, LogExporterBehavior.Console.mergeWith(LogExporterBehavior.Console))
    }
}
