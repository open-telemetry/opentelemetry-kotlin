package io.opentelemetry.kotlin.encode

import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import kotlin.test.Test
import kotlin.test.assertTrue

internal class JsonSpanEncoderTest {

    @Test
    fun `should successfully encode span data in JSON format`() {
        // given
        val encoder = JsonSpanEncoder()

        // when
        val result = encoder.encode(FakeSpanData())

        // then
        assertTrue { result.any() }
    }
}
