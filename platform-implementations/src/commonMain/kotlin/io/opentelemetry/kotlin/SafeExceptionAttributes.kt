package io.opentelemetry.kotlin

/** Exception fields extracted without allowing a hostile [Throwable] to escape. */
public data class SafeExceptionAttributes(
    val type: String?,
    val message: String?,
    val stacktrace: String?,
)

/** Extracts bounded exception fields without trusting accessors on [throwable]. */
public expect fun safeExceptionAttributes(throwable: Throwable): SafeExceptionAttributes

internal fun safeExceptionAttributesFallback(throwable: Throwable): SafeExceptionAttributes {
    val type = try {
        throwable.exceptionType()
    } catch (_: Throwable) {
        null
    }
    val fallback = type ?: "Throwable"
    val message = try {
        throwable.message?.let(::truncateUtf8)
    } catch (_: Throwable) {
        fallback
    }
    // Common Throwable APIs do not expose causes, suppressed exceptions, and frames consistently
    // enough to format them with bounded traversal. Build a bounded summary instead of invoking
    // the potentially hostile and unbounded stackTraceToString().
    val stacktrace = buildString {
        append(fallback)
        message?.takeIf { it.isNotEmpty() && it != fallback }?.let {
            append(": ").append(it)
        }
    }
    return SafeExceptionAttributes(type, message, stacktrace)
}

internal const val MAX_SAFE_EXCEPTION_MESSAGE_LENGTH: Int = 1024

internal fun truncateUtf8(value: String): String {
    var index = 0
    var byteCount = 0
    while (index < value.length) {
        val first = value[index].code
        val isSurrogatePair = first in 0xD800..0xDBFF &&
            index + 1 < value.length && value[index + 1].code in 0xDC00..0xDFFF
        val bytes = when {
            isSurrogatePair -> 4
            first <= 0x7F -> 1
            first <= 0x7FF -> 2
            else -> 3
        }
        if (byteCount + bytes > MAX_SAFE_EXCEPTION_MESSAGE_LENGTH) {
            break
        }
        byteCount += bytes
        index += when {
            isSurrogatePair -> 2
            else -> 1
        }
    }
    return when (index) {
        value.length -> value
        else -> value.substring(0, index)
    }
}
