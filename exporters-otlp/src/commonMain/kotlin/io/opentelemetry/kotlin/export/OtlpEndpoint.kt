package io.opentelemetry.kotlin.export

internal enum class OtlpEndpoint(val path: String) {
    Logs("v1/logs"),
    Traces("v1/traces")
}
