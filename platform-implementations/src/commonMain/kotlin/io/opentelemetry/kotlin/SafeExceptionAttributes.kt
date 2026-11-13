package io.opentelemetry.kotlin

/** Safe exception fields shared by SDK modules. */
public class SafeExceptionAttributes internal constructor(
    public val type: String?,
    public val message: String?,
    public val stackTrace: String,
)

/** Reads exception details without allowing a hostile Throwable to escape. */
public expect fun safeExceptionAttributes(throwable: Throwable): SafeExceptionAttributes

internal fun safeExceptionAttributesFallback(throwable: Throwable): SafeExceptionAttributes {
    val className = try {
        throwable::class.simpleName ?: "Throwable"
    } catch (_: Throwable) {
        "Throwable"
    }
    val type = try {
        throwable.exceptionType()
    } catch (_: Throwable) {
        className
    }
    val message = try {
        throwable.message?.take(1024)
    } catch (_: Throwable) {
        className
    }
    val header = if (message == null) className else "$className: $message"
    return SafeExceptionAttributes(type, message, header)
}