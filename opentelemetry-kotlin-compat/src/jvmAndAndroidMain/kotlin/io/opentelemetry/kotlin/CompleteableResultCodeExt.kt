package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.export.OperationResultCode

@OptIn(ExperimentalApi::class)
internal fun OtelJavaCompletableResultCode.toOperationResultCode(): OperationResultCode = when (this) {
    OtelJavaCompletableResultCode.ofSuccess() -> OperationResultCode.Success
    else -> OperationResultCode.Failure
}
