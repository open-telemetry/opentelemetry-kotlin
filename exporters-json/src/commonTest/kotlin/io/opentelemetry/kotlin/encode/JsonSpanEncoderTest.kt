package io.opentelemetry.kotlin.encode

import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import kotlin.test.Test
import kotlin.test.assertTrue

internal class JsonSpanEncoderTest {

    @Test
    fun `should successfully encode span data in JSON format`() {
        //given
        val foo = JsonSpanEncoder()

        //when
        val result = foo.encode(FakeSpanData())

        //then
        assertTrue { result.count() > 0 }
    }
}