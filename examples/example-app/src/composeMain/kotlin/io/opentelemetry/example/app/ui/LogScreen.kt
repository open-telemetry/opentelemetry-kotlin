@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalApi::class)

package io.opentelemetry.example.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import io.opentelemetry.example.app.AttributeType
import io.opentelemetry.example.app.LogFormState
import io.opentelemetry.example.app.ui.components.AttributeEditor
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import kotlinx.coroutines.delay

@Composable
fun LogScreen(otel: OpenTelemetry) {
    var formState by remember { mutableStateOf(LogFormState()) }
    var severityExpanded by remember { mutableStateOf(false) }
    var showSuccessFeedback by remember { mutableStateOf(false) }
    val severities = SeverityNumber.entries.map { it.name }

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
            value = formState.body,
            onValueChange = { formState = formState.copy(body = it) },
            label = { Text("Log Body") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 2,
        )

        ExposedDropdownMenuBox(
            expanded = severityExpanded,
            onExpandedChange = { severityExpanded = it },
        ) {
            OutlinedTextField(
                value = formState.severityNumber,
                onValueChange = {},
                readOnly = true,
                label = { Text("Severity") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = severityExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                singleLine = true,
            )
            ExposedDropdownMenu(
                expanded = severityExpanded,
                onDismissRequest = { severityExpanded = false },
            ) {
                severities.forEach { sev ->
                    DropdownMenuItem(
                        text = { Text(sev) },
                        onClick = {
                            formState = formState.copy(severityNumber = sev)
                            severityExpanded = false
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = formState.severityText,
            onValueChange = { formState = formState.copy(severityText = it) },
            label = { Text("Severity Text (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        AttributeEditor(
            attributes = formState.attributes,
            onAttributesChanged = { formState = formState.copy(attributes = it) },
        )

        Button(onClick = {
            emitLog(otel, formState)
            showSuccessFeedback = true
        }) {
            Text("Emit Log")
        }

        if (showSuccessFeedback) {
            Text("Success!", style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, color = androidx.compose.ui.graphics.Color.Green)
        }
    }
}

private fun emitLog(otel: OpenTelemetry, form: LogFormState) {
    otel.loggerProvider.getLogger(AppConfig.APP_NAME).emit(
        body = form.body.ifBlank { null },
        timestamp = form.timestamp.toLongOrNull(),
        observedTimestamp = form.observedTimestamp.toLongOrNull(),
        severityNumber = parseSeverityNumber(form.severityNumber),
        severityText = form.severityText.ifBlank { null },
        attributes = {
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
        })
}

private fun parseSeverityNumber(value: String): SeverityNumber? =
    SeverityNumber.entries.find { it.name == value }
