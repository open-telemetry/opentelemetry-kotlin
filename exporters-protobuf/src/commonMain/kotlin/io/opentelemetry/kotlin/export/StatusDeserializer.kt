package io.opentelemetry.kotlin.export

import com.squareup.wire.ProtoAdapter
import com.squareup.wire.ProtoReader
import okio.Buffer

private const val STATUS_MESSAGE_TAG = 2

/**
 * Parses `Status.message` from the status body that OTLP/HTTP servers return for
 * 4xx and 5xx responses, returning null when it is absent or can't be decoded.
 *
 * https://opentelemetry.io/docs/specs/otlp/#failures-1
 */
fun ByteArray.deserializeStatusMessage(): String? = runCatching {
    val reader = ProtoReader(Buffer().write(this))
    val token = reader.beginMessage()
    var message: String? = null
    while (true) {
        when (val tag = reader.nextTag()) {
            -1 -> break
            STATUS_MESSAGE_TAG -> message = ProtoAdapter.STRING.decode(reader)
            else -> reader.readUnknownField(tag)
        }
    }
    reader.endMessageAndGetUnknownFields(token)
    message
}.getOrNull()?.takeIf { it.isNotEmpty() }
