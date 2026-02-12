package io.opentelemetry.example.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.opentelemetry.example.app.EventEntry

@Composable
fun EventEditor(
    events: List<EventEntry>,
    onEventsChanged: (List<EventEntry>) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Events", style = MaterialTheme.typography.labelLarge)

        events.forEachIndexed { index, event ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .padding(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = event.name,
                        onValueChange = { newName ->
                            onEventsChanged(
                                events.toMutableList().also {
                                    it[index] = event.copy(name = newName)
                                }
                            )
                        },
                        label = { Text("Event Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = event.timestamp,
                        onValueChange = { newTs ->
                            onEventsChanged(
                                events.toMutableList().also {
                                    it[index] = event.copy(timestamp = newTs)
                                }
                            )
                        },
                        label = { Text("Timestamp (ns)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    IconButton(onClick = {
                        onEventsChanged(events.toMutableList().also { it.removeAt(index) })
                    }) {
                        Text("X")
                    }
                }

                AttributeEditor(
                    attributes = event.attributes,
                    onAttributesChanged = { newAttrs ->
                        onEventsChanged(
                            events.toMutableList().also {
                                it[index] = event.copy(attributes = newAttrs)
                            }
                        )
                    },
                )
            }
        }

        Button(onClick = {
            onEventsChanged(events + EventEntry())
        }) {
            Text("Add Event")
        }
    }
}
