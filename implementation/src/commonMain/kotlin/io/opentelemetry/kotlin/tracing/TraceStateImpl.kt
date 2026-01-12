package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.model.TraceState

@ExperimentalApi
internal class TraceStateImpl private constructor(
    private val data: Map<String, String>
) : TraceState {

    companion object {
        fun create(): TraceState = TraceStateImpl(emptyMap())
    }

    override fun get(key: String): String? = data[key]

    override fun asMap(): Map<String, String> = data.toMap()

    override fun put(key: String, value: String): TraceState {
        if (!isValidKey(key) || !isValidValue(value)) {
            return this
        }

        return TraceStateImpl(data + (key to value))
    }

    override fun remove(key: String): TraceState {
        if (!data.containsKey(key)) {
            return this
        }

        return TraceStateImpl(data - key)
    }

    private fun isValidKey(key: String): Boolean {
        if (key.isBlank() || key.length > 256) {
            return false
        }

        val parts = key.split('@')

        return when (parts.size) {
            1 -> isValidSimpleKey(key)
            2 -> isValidMultiTenantKey(parts[0], parts[1])
            else -> false // Invalid: multiple @ symbols
        }
    }

    private fun isValidSimpleKey(key: String): Boolean {
        return key.matches(Regex("^[a-z0-9][a-z0-9_*/-]*$"))
    }

    private fun isValidMultiTenantKey(tenant: String, system: String): Boolean {
        // Tenant: max 241 chars (1 + 0*240), starts with lowercase letter or digit
        if (tenant.length > 241 || !tenant.matches(Regex("^[a-z0-9][a-z0-9_*/-]*$"))) {
            return false
        }

        // System: max 14 chars, starts with lowercase letter
        if (system.length > 14 || !system.matches(Regex("^[a-z][a-z0-9_*/-]*$"))) {
            return false
        }

        return true
    }

    private fun isValidValue(value: String): Boolean {
        // W3C TraceState value validation
        // Value must be max 256 characters, printable ASCII except comma and equals
        return value.length <= 256 && value.all { it.isValidTraceStateChar() }
    }

    private fun Char.isValidTraceStateChar(): Boolean {
        // Printable ASCII (0x20-0x7E) except comma (0x2C) and equals (0x3D)
        return this.code in 0x20..0x7E && this != ',' && this != '='
    }
}
