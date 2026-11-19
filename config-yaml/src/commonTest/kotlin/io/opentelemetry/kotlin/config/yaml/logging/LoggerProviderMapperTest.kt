package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.behavior.logging.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.LoggerProviderBehavior
import io.opentelemetry.kotlin.behavior.logging.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.SimpleLogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.LogRecordExporter
import io.opentelemetry.kotlin.config.schema.model.LogRecordLimits
import io.opentelemetry.kotlin.config.schema.model.LogRecordProcessor
import io.opentelemetry.kotlin.config.schema.model.LoggerProvider
import io.opentelemetry.kotlin.config.schema.model.OtlpHttpExporter
import io.opentelemetry.kotlin.config.schema.model.SimpleLogRecordProcessor
import kotlin.collections.emptyList
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LoggerProviderMapperTest {
    @Test
    fun unsetIfNoMappedFields() {
        assertEquals(
            LoggerProviderBehavior(processor = LogRecordProcessorBehavior()),
            LoggerProvider(emptyList()).toBehavior()
        )
    }

    @Test
    fun mapsLoggerProviderFields() {
        val loggerProvider = LoggerProvider(
            processors = listOf(
                LogRecordProcessor(
                    simple = SimpleLogRecordProcessor(
                        exporter = LogRecordExporter(
                            otlpHttp = OtlpHttpExporter(
                                endpoint = "http://localhost:4318/v1/logs"
                            )
                        )
                    )
                )
            ),
            limits = LogRecordLimits(
                attributeValueLengthLimit = 1,
                attributeCountLimit = 2
            )
        )
        val behavior = LoggerProviderBehavior(
            logLimits = LogLimitsBehavior(
                attributeValueLengthLimit = 1,
                attributeCountLimit = 2
            ),
            processor = LogRecordProcessorBehavior(
                simple = SimpleLogRecordProcessorBehavior(
                    exporter = LogRecordExporterBehavior(
                        http = OtlpHttpLogsExporterBehavior(
                            endpoint = "http://localhost:4318/v1/logs"
                        )
                    )
                )
            )
        )
        assertEquals(
            behavior,
            loggerProvider.toBehavior()
        )
    }
}
