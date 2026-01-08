package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.export.OperationResultCode
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalApi::class)
internal class CompleteableResultCodeExtTest {

    @Test
    fun `test conversion`() {
        assertEquals(
            OperationResultCode.Success,
            OtelJavaCompletableResultCode.ofSuccess().toOperationResultCode()
        )
        assertEquals(
            OperationResultCode.Failure,
            OtelJavaCompletableResultCode.ofFailure().toOperationResultCode()
        )
    }
}
