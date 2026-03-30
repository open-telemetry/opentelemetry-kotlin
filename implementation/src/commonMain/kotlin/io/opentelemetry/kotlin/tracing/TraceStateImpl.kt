package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
internal class TraceStateImpl private constructor(
    private val data: Map<String, String>
) : TraceState {

    companion object {
        private const val MAX_ENTRIES = 32
        private const val MAX_KEY_LENGTH = 256
        private const val MAX_VALUE_LENGTH = 256
        private const val MAX_TENANT_LENGTH = 241
        private const val MAX_SYSTEM_LENGTH = 14
        private const val CHR_MIN = 0x20
        private const val CHR_MAX = 0x7E
        private const val NBLK_CHR_MIN = 0x21
        private val SIMPLE_KEY_REGEX = Regex("^[a-z][a-z0-9_*/-]*$")
        private val TENANT_ID_REGEX = Regex("^[a-z0-9][a-z0-9_*/-]*$")

        fun create(): TraceState = TraceStateImpl(emptyMap())
    }

    override fun get(key: String): String? = data[key]

    override fun asMap(): Map<String, String> = data.toMap()

    override fun put(key: String, value: String): TraceState {
        if (!isValidKey(key) || !isValidValue(value)) {
            return this
        }
        if (data.size >= MAX_ENTRIES && !data.containsKey(key)) {
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
        if (key.isBlank() || key.length > MAX_KEY_LENGTH) {
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
        return key.matches(SIMPLE_KEY_REGEX)
    }

    private fun isValidMultiTenantKey(tenant: String, system: String): Boolean {
        // Tenant: max 241 chars (1 + 0*240), starts with lowercase letter or digit
        if (tenant.length > MAX_TENANT_LENGTH || !tenant.matches(TENANT_ID_REGEX)) {
            return false
        }

        // System: max 14 chars, starts with lowercase letter
        if (system.length > MAX_SYSTEM_LENGTH || !system.matches(SIMPLE_KEY_REGEX)) {
            return false
        }

        return true
    }

    private fun isValidValue(value: String): Boolean {
        // W3C TraceState value validation
        // Value must be max 256 characters, printable ASCII except comma and equals
        if (value.isEmpty() || value.length > MAX_VALUE_LENGTH) {
            return false
        }
        if (!value.all { it.isValidTraceStateChar() }) {
            return false
        }
        return value.last().isNonBlankTraceStateChar()
    }

    private fun Char.isValidTraceStateChar(): Boolean {
        // Printable ASCII (0x20-0x7E) except comma (0x2C) and equals (0x3D)
        return this.code in CHR_MIN..CHR_MAX && this != ',' && this != '='
    }

    private fun Char.isNonBlankTraceStateChar(): Boolean {
        // nblk-chr = 0x21-0x7E except comma (0x2C) and equals (0x3D)
        return this.code in NBLK_CHR_MIN..CHR_MAX && this != ',' && this != '='
    }
}
