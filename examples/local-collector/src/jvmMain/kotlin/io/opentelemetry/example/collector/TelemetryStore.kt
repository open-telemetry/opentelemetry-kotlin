package io.opentelemetry.example.collector

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds the most recent telemetry in memory. Newest items come first and the oldest are dropped
 * once [capacity] is reached.
 */
class TelemetryStore(private val capacity: Int = 5_000) {

    private val _spans = MutableStateFlow<List<ReceivedSpan>>(emptyList())
    val spans: StateFlow<List<ReceivedSpan>> = _spans.asStateFlow()

    private val _logs = MutableStateFlow<List<ReceivedLog>>(emptyList())
    val logs: StateFlow<List<ReceivedLog>> = _logs.asStateFlow()

    fun addSpans(items: List<ReceivedSpan>) {
        _spans.update { (items.reversed() + it).take(capacity) }
    }

    fun addLogs(items: List<ReceivedLog>) {
        _logs.update { (items.reversed() + it).take(capacity) }
    }

    fun clear() {
        _spans.value = emptyList()
        _logs.value = emptyList()
    }
}
