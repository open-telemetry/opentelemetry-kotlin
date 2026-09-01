package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.reportUserCodeError
import kotlinx.coroutines.CoroutineExceptionHandler

/**
 * Creates a [CoroutineExceptionHandler] for telemetry export scopes.
 *
 * Export happens on fire-and-forget coroutines. Without a handler, any uncaught exception on a
 * scope backed by a [kotlinx.coroutines.SupervisorJob] is routed to the platform default handler,
 * which crashes the host application (fatal on Android/JVM). Telemetry must never destabilize the
 * app, so unhandled failures are logged and swallowed instead.
 *
 * [kotlinx.coroutines.CancellationException] is never routed here by the framework, so normal
 * cancellation (e.g. on shutdown) is unaffected.
 */
public fun telemetryExceptionHandler(context: String, sdkErrorHandler: SdkErrorHandler): CoroutineExceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        sdkErrorHandler.reportUserCodeError(throwable, "$context coroutine failed")
    }
