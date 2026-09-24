package io.opentelemetry.kotlin.config.envar.reader

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Invalid
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value

/**
 * Reads environment variables through [delegate], reports invalid values, and applies the fallback
 * required by each type.
 */
@ExperimentalApi
class ReportingEnvVarReader(
    private val delegate: EnvVarReader,
    private val onWarning: (EnvVarReadWarning) -> Unit,
) {

    fun readString(name: String): String? =
        delegate.readString(name)
            .resolveNullable()

    fun readNonNegativeInt(name: String): Int? =
        delegate.readNonNegativeInt(name)
            .resolveNullable()

    fun readBoolean(name: String): Boolean =
        delegate.readBoolean(name)
            .resolveNullable()
            ?: false

    internal fun <T> readStringAndTransform(
        name: String,
        transform: (String) -> EnvVarReadResult<T?>,
    ): T? =
        delegate.readString(name)
            .flatMap(transform)
            .resolveNullable()

    private fun <T> EnvVarReadResult<T>.resolveNullable(): T? =
        when (this) {
            is Value -> {
                this.warning?.let(onWarning)
                this.value
            }
            EnvVarReadResult.Unset -> {
                null
            }
            is Invalid -> {
                onWarning(this.warning)
                null
            }
        }
}
