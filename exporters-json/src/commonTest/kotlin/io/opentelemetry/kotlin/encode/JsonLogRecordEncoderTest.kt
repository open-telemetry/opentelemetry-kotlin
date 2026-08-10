package io.opentelemetry.kotlin.encode

import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import kotlin.test.Test
import kotlin.test.assertTrue

internal class JsonLogRecordEncoderTest {

    @Test
    fun `should successfully encode a log record data in JSON format`() {
        // given
        val encoder = JsonLogRecordEncoder()

        // when
        val result = encoder.encode(FakeReadableLogRecord())

        // then
        assertTrue { result.any() }
    }
}
