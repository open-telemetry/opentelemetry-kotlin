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
        throwable.message?.let(::truncateUtf8)
    } catch (_: Throwable) {
        className
    }
    val header = buildString {
        append(className)
        message?.takeIf { it.isNotEmpty() && it != className }?.let {
            append(": ").append(it)
        }
    }
    return SafeExceptionAttributes(type, message, header)
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
        index += if (isSurrogatePair) 2 else 1
    }
    return if (index == value.length) value else value.substring(0, index)
}
