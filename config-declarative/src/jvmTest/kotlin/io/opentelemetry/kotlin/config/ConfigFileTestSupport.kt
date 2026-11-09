package io.opentelemetry.kotlin.config

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

internal fun writeConfigFile(contents: String): String {
    val file = File.createTempFile("opentelemetry-config", ".yaml")
    file.deleteOnExit()
    file.writeText(contents)
    return file.absolutePath
}

internal fun captureStdout(block: (ByteArrayOutputStream) -> Unit): String {
    val buffer = ByteArrayOutputStream()
    val previous = System.out
    System.setOut(PrintStream(buffer, true))
    try {
        block(buffer)
    } finally {
        System.setOut(previous)
    }
    return buffer.toString()
}

/**
 * Waits for [text] to appear in [buffer]. Simple processors export asynchronously, and their
 * forceFlush() does not wait for exports already in flight.
 */
internal fun awaitOutput(buffer: ByteArrayOutputStream, text: String, timeoutMs: Long = 5_000): Boolean {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < deadline) {
        if (buffer.toString().contains(text)) {
            return true
        }
        Thread.sleep(10)
    }
    return buffer.toString().contains(text)
}

internal fun captureJul(loggerName: String, block: (List<String>) -> Unit): List<String> {
    val logger = Logger.getLogger(loggerName)
    val messages = mutableListOf<String>()
    val handler = object : Handler() {
        override fun publish(record: LogRecord) {
            messages.add(record.message.orEmpty())
        }

        override fun flush() {}

        override fun close() {}
    }
    val previousLevel = logger.level
    val previousUseParent = logger.useParentHandlers
    logger.addHandler(handler)
    logger.level = Level.ALL
    logger.useParentHandlers = false
    try {
        block(messages)
    } finally {
        logger.removeHandler(handler)
        logger.level = previousLevel
        logger.useParentHandlers = previousUseParent
    }
    return messages
}

internal const val ATTRIBUTE_LIMIT_CONFIG_FILE = """
file_format: "1.0"
attribute_limits:
  attribute_count_limit: 64
"""

internal const val CONSOLE_CONFIG_FILE = """
file_format: "1.0"
tracer_provider:
  processors:
    - simple:
        exporter:
          console: {}
logger_provider:
  processors:
    - simple:
        exporter:
          console: {}
"""

internal const val BATCH_CONSOLE_CONFIG_FILE = """
file_format: "1.0"
tracer_provider:
  processors:
    - batch:
        schedule_delay: 60000
        exporter:
          console: {}
logger_provider:
  processors:
    - batch:
        schedule_delay: 60000
        exporter:
          console: {}
"""

internal const val ALWAYS_OFF_SAMPLER_CONFIG_FILE = """
file_format: "1.0"
tracer_provider:
  processors: []
  sampler:
    always_off: {}
"""
