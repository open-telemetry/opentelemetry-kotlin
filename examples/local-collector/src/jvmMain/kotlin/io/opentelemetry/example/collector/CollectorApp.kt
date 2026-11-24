package io.opentelemetry.example.collector

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun CollectorApp(store: TelemetryStore) {
    val spans by store.spans.collectAsState()
    val logs by store.logs.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var filter by remember { mutableStateOf("") }
    var selectedSpan by remember { mutableStateOf<ReceivedSpan?>(null) }
    var selectedLog by remember { mutableStateOf<ReceivedLog?>(null) }

    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column {
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TabRow(selectedTabIndex = tab, modifier = Modifier.width(320.dp)) {
                        Tab(tab == 0, onClick = { tab = 0 }, text = { Text("Spans (${spans.size})") })
                        Tab(tab == 1, onClick = { tab = 1 }, text = { Text("Logs (${logs.size})") })
                    }
                    OutlinedTextField(
                        value = filter,
                        onValueChange = { filter = it },
                        placeholder = { Text("Filter by name, service, body or trace ID") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = {
                        store.clear()
                        selectedSpan = null
                        selectedLog = null
                    }) { Text("Clear") }
                }
                HorizontalDivider()
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(0.45f).fillMaxHeight()) {
                        if (tab == 0) {
                            val visible = spans.filter { it.matches(filter) }
                            ItemList(visible, selectedSpan, { selectedSpan = it }) { SpanRow(it) }
                        } else {
                            val visible = logs.filter { it.matches(filter) }
                            ItemList(visible, selectedLog, { selectedLog = it }) { LogRow(it) }
                        }
                    }
                    VerticalDivider()
                    Box(Modifier.weight(0.55f).fillMaxHeight()) {
                        when {
                            tab == 0 && selectedSpan != null -> SpanDetail(selectedSpan!!)
                            tab == 1 && selectedLog != null -> LogDetail(selectedLog!!)
                            else -> Placeholder(if (spans.isEmpty() && logs.isEmpty()) {
                                "Waiting for data. Point your OTLP exporter at http://localhost:4318"
                            } else {
                                "Select an item to inspect it"
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> ItemList(
    items: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    row: @Composable (T) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(items) { item ->
            val bg = if (item == selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
            Box(Modifier.fillMaxWidth().background(bg).clickable { onSelect(item) }.padding(8.dp)) {
                row(item)
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun SpanRow(span: ReceivedSpan) {
    Column {
        Row {
            Text(span.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("%.2f ms".format(span.durationMs), style = MaterialTheme.typography.bodySmall)
        }
        Text(
            "${span.serviceName} · ${span.kind} · ${span.status}",
            style = MaterialTheme.typography.bodySmall,
            color = if (span.status.startsWith("ERROR")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LogRow(log: ReceivedLog) {
    Column {
        Row {
            Text("[${log.severity}]", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(end = 8.dp))
            Text(log.body.ifEmpty { log.eventName }, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("${log.serviceName} · ${formatNanos(log.timeNanos)}", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SpanDetail(span: ReceivedSpan) {
    DetailPane(span.name) {
        Section(
            "Span",
            listOf(
                "trace_id" to span.traceId,
                "span_id" to span.spanId,
                "parent_span_id" to span.parentSpanId.ifEmpty { "(root)" },
                "kind" to span.kind,
                "status" to span.status,
                "start" to formatNanos(span.startNanos),
                "end" to formatNanos(span.endNanos),
                "duration" to "%.3f ms".format(span.durationMs),
                "scope" to span.scope,
            )
        )
        Section("Attributes", span.attributes)
        span.events.forEach { event ->
            Section("Event: ${event.name} @ ${formatNanos(event.timeNanos)}", event.attributes)
        }
        if (span.links.isNotEmpty()) {
            Section("Links", span.links.mapIndexed { i, link -> "#$i" to link })
        }
        Section("Resource", span.resource)
    }
}

@Composable
private fun LogDetail(log: ReceivedLog) {
    DetailPane(log.eventName.ifEmpty { "Log record" }) {
        Section(
            "Log",
            listOf(
                "severity" to log.severity,
                "time" to formatNanos(log.timeNanos),
                "body" to log.body,
                "event_name" to log.eventName,
                "trace_id" to log.traceId,
                "span_id" to log.spanId,
                "scope" to log.scope,
            )
        )
        Section("Attributes", log.attributes)
        Section("Resource", log.resource)
    }
}

@Composable
private fun DetailPane(title: String, content: @Composable () -> Unit) {
    SelectionContainer {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun Section(title: String, rows: List<Pair<String, String>>) {
    Spacer(Modifier.height(12.dp))
    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    HorizontalDivider(Modifier.padding(vertical = 4.dp))
    if (rows.isEmpty()) {
        Text("(none)", style = MaterialTheme.typography.bodySmall)
    }
    rows.forEach { (key, value) ->
        Row(Modifier.padding(vertical = 2.dp)) {
            Text(key, fontFamily = FontFamily.Monospace, modifier = Modifier.width(200.dp), style = MaterialTheme.typography.bodySmall)
            Text(value, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Placeholder(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun ReceivedSpan.matches(filter: String): Boolean =
    filter.isBlank() || listOf(name, serviceName, traceId, spanId).any { it.contains(filter, ignoreCase = true) }

private fun ReceivedLog.matches(filter: String): Boolean =
    filter.isBlank() || listOf(body, serviceName, eventName, traceId, severity).any { it.contains(filter, ignoreCase = true) }
