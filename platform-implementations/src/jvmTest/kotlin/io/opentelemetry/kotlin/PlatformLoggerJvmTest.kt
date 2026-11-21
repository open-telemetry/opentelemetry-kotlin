package io.opentelemetry.kotlin

import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class PlatformLoggerJvmTest {

    private val logger = Logger.getLogger("io.opentelemetry.kotlin")
    private val originalLevel = logger.level
    private val originalUseParentHandlers = logger.useParentHandlers
    private val records = mutableListOf<LogRecord>()
    private val handler = object : Handler() {
        override fun publish(record: LogRecord) {
            records.add(record)
        }

        override fun flush() = Unit
        override fun close() = Unit
    }

    @BeforeTest
    fun setUp() {
        logger.level = Level.ALL
        logger.useParentHandlers = false
        handler.level = Level.ALL
        logger.addHandler(handler)
    }

    @AfterTest
    fun tearDown() {
        logger.removeHandler(handler)
        logger.level = originalLevel
        logger.useParentHandlers = originalUseParentHandlers
    }

    @Test
    fun routesSeverityMessageAndThrowableToJavaLogging() {
        val failure = IllegalStateException("export failed")
        platformLog("information", PlatformLogLevel.INFO)
        platformLog("warning", PlatformLogLevel.WARNING)
        platformLog("error", PlatformLogLevel.ERROR, failure)

        assertEquals(listOf(Level.INFO, Level.WARNING, Level.SEVERE), records.map { it.level })
        assertEquals(listOf("information", "warning", "error"), records.map { it.message })
        assertEquals(listOf(logger.name, logger.name, logger.name), records.map { it.loggerName })
        assertNull(records[0].thrown)
        assertNull(records[1].thrown)
        assertSame(failure, records[2].thrown)
    }

    @Test
    fun respectsTheConfiguredJavaLoggingLevel() {
        logger.level = Level.SEVERE
        platformLog("filtered info", PlatformLogLevel.INFO)
        platformLog("filtered warning", PlatformLogLevel.WARNING)
        platformLog("visible error", PlatformLogLevel.ERROR)

        assertEquals(listOf("visible error"), records.map { it.message })
    }
}
