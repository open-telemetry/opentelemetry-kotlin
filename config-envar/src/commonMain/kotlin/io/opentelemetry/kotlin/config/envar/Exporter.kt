package io.opentelemetry.kotlin.config.envar

enum class Exporter(val value: String) {
    CONSOLE("console"),
    OTLP("otlp"),
    LOGGING("logging"),
    NONE("none"),
    OTLP_STDOUT("otlp/stdout");

    companion object {
        fun fromValue(value: String): Exporter? = Exporter.entries.find { it.value == value }
    }
}
