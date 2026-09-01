package io.opentelemetry.kotlin.propagation

/**
 * Validates the keys and values of W3C `tracestate` entries, and caps the number of entries a
 * `tracestate` may hold.
 *
 * https://www.w3.org/TR/trace-context/#tracestate-header
 *
 * This complements [W3CTraceStateCodec], which handles the list structure and combined-header
 * size limits. This type validates key/value characters and whether a new entry may be put.
 */
public object W3CTraceStateValidator {

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

    /**
     * Whether [key] satisfies https://www.w3.org/TR/trace-context/#key
     */
    public fun isValidKey(key: String): Boolean {
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

    /**
     * Whether [value] satisfies https://www.w3.org/TR/trace-context/#value
     */
    public fun isValidValue(value: String): Boolean {
        // Value must be max 256 characters, printable ASCII except comma and equals
        if (value.isEmpty() || value.length > MAX_VALUE_LENGTH) {
            return false
        }
        if (!value.all { it.isValidTraceStateChar() }) {
            return false
        }
        return value.last().isNonBlankTraceStateChar()
    }

    /**
     * Whether [key] and [value] may be added to [entries]: the key and value must both be valid,
     * and adding a new key must not exceed the maximum number of entries.
     */
    public fun canPut(entries: Map<String, String>, key: String, value: String): Boolean {
        if (!isValidKey(key) || !isValidValue(value)) {
            return false
        }
        return entries.containsKey(key) || entries.size < MAX_ENTRIES
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

    private fun Char.isValidTraceStateChar(): Boolean {
        // Printable ASCII (0x20-0x7E) except comma (0x2C) and equals (0x3D)
        return this.code in CHR_MIN..CHR_MAX && this != ',' && this != '='
    }

    private fun Char.isNonBlankTraceStateChar(): Boolean {
        // nblk-chr = 0x21-0x7E except comma (0x2C) and equals (0x3D)
        return this.code in NBLK_CHR_MIN..CHR_MAX && this != ',' && this != '='
    }
}
