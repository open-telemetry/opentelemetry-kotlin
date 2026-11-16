package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class PerOperationSamplerTest {

    @Test
    fun testPerOperationShutsDownDefaultAndOperationSamplers() = runTest {
        val default = FakeSampler()
        val opA = FakeSampler()
        val opB = FakeSampler()
        val sampler = PerOperationSampler(default, mapOf("a" to opA, "b" to opB, "c" to default))

        assertEquals(OperationResultCode.Success, sampler.shutdown())
        assertEquals(1, default.shutdownCount)
        assertEquals(1, opA.shutdownCount)
        assertEquals(1, opB.shutdownCount)
    }
}
