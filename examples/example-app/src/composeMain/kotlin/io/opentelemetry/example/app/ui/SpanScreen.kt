@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalApi::class)

package io.opentelemetry.example.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.opentelemetry.example.app.AppConfig
import kotlinx.coroutines.delay
import io.opentelemetry.example.app.AttributeType
import io.opentelemetry.example.app.SpanFormState
import io.opentelemetry.example.app.ui.components.AttributeEditor
import io.opentelemetry.example.app.ui.components.EventEditor
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.context.Scope
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanKind

@Composable
fun SpanScreen(
    otel: OpenTelemetry,
    activeSpan: Span? = null,
    onActiveSpanChanged: (Span?) -> Unit = {},
) {
    var formState by remember { mutableStateOf(SpanFormState()) }
    var kindExpanded by remember { mutableStateOf(false) }
    var showSuccessFeedback by remember { mutableStateOf(false) }
    var activeScope by remember { mutableStateOf<Scope?>(null) }
    val spanKinds = SpanKind.entries.map { it.name }

    LaunchedEffect(showSuccessFeedback) {
        if (showSuccessFeedback) {
            delay(2000)
            showSuccessFeedback = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = formState.name,
            onValueChange = { formState = formState.copy(name = it) },
            label = { Text("Span Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        ExposedDropdownMenuBox(
            expanded = kindExpanded,
            onExpandedChange = { kindExpanded = it },
        ) {
            OutlinedTextField(
                value = formState.spanKind,
                onValueChange = {},
                readOnly = true,
                label = { Text("Span Kind") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = kindExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                singleLine = true,
            )
            ExposedDropdownMenu(
                expanded = kindExpanded,
                onDismissRequest = { kindExpanded = false },
            ) {
                spanKinds.forEach { kind ->
                    DropdownMenuItem(
                        text = { Text(kind) },
                        onClick = {
                            formState = formState.copy(spanKind = kind)
                            kindExpanded = false
                        },
                    )
                }
            }
        }

        AttributeEditor(
            attributes = formState.attributes,
            onAttributesChanged = { formState = formState.copy(attributes = it) },
        )

        EventEditor(
            events = formState.events,
            onEventsChanged = { formState = formState.copy(events = it) },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = formState.setAsImplicitContext,
                onCheckedChange = { formState = formState.copy(setAsImplicitContext = it) },
            )
            Text("Set as implicit context")
        }

        OutlinedTextField(
            value = formState.startTimestamp,
            onValueChange = { formState = formState.copy(startTimestamp = it) },
            label = { Text("Start Timestamp (ns, optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (activeSpan == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = {
                    val (span, scope) = startSpan(otel, formState)
                    span.end()
                    scope?.detach()
                    showSuccessFeedback = true
                }) {
                    Text("Create & End Span")
                }
                OutlinedButton(onClick = {
                    val (span, scope) = startSpan(otel, formState)
                    activeScope = scope
                    onActiveSpanChanged(span)
                }) {
                    Text("Create Span (keep open)")
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = {
                    activeSpan.end()
                    activeScope?.detach()
                    onActiveSpanChanged(null)
                    activeScope = null
                    formState = SpanFormState()
                    showSuccessFeedback = true
                }) {
                    Text("End Span")
                }
            }
        }

        if (showSuccessFeedback) {
            Text("Success!", style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, color = androidx.compose.ui.graphics.Color.Green)
        }
    }
}

@ExperimentalApi
private fun startSpan(otel: OpenTelemetry, form: SpanFormState): Pair<Span, Scope?> {
    val startTs = form.startTimestamp.toLongOrNull()
    val span = otel.tracerProvider.getTracer(AppConfig.APP_NAME).startSpan(
        name = form.name,
        spanKind = parseSpanKind(form.spanKind),
        startTimestamp = startTs,
        action = {
            form.attributes.forEach { attr ->
                if (attr.key.isNotBlank()) {
                    when (attr.type) {
                        AttributeType.STRING -> setStringAttribute(attr.key, attr.value)
                        AttributeType.LONG -> attr.value.toLongOrNull()
                            ?.let { setLongAttribute(attr.key, it) }

                        AttributeType.DOUBLE -> attr.value.toDoubleOrNull()
                            ?.let { setDoubleAttribute(attr.key, it) }

                        AttributeType.BOOLEAN -> setBooleanAttribute(
                            attr.key,
                            attr.value.toBoolean()
                        )
                    }
                }
            }
            form.events.forEach { event ->
                if (event.name.isNotBlank()) {
                    val eventTs = event.timestamp.toLongOrNull()
                    if (eventTs != null) {
                        addEvent(name = event.name, timestamp = eventTs) {
                            event.attributes.forEach { attr ->
                                if (attr.key.isNotBlank()) {
                                    when (attr.type) {
                                        AttributeType.STRING -> setStringAttribute(
                                            attr.key,
                                            attr.value
                                        )

                                        AttributeType.LONG -> attr.value.toLongOrNull()
                                            ?.let { setLongAttribute(attr.key, it) }

                                        AttributeType.DOUBLE -> attr.value.toDoubleOrNull()
                                            ?.let { setDoubleAttribute(attr.key, it) }

                                        AttributeType.BOOLEAN -> setBooleanAttribute(
                                            attr.key,
                                            attr.value.toBoolean()
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        addEvent(event.name)
                    }
                }
            }
        })

    // Set span as implicit context if requested
    val scope = if (form.setAsImplicitContext) {
        val contextFactory = otel.contextFactory
        val currentContext = contextFactory.implicitContext()
        val spanContext = contextFactory.storeSpan(currentContext, span)
        spanContext.attach()
    } else {
        null
    }

    return Pair(span, scope)
}

private fun parseSpanKind(value: String): SpanKind = SpanKind.valueOf(value)
