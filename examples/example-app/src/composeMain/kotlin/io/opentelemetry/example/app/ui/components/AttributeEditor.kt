package io.opentelemetry.example.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.opentelemetry.example.app.AttributeEntry
import io.opentelemetry.example.app.AttributeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttributeEditor(
    attributes: List<AttributeEntry>,
    onAttributesChanged: (List<AttributeEntry>) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Attributes", style = MaterialTheme.typography.labelLarge)

        attributes.forEachIndexed { index, attr ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = attr.key,
                    onValueChange = { newKey ->
                        onAttributesChanged(
                            attributes.toMutableList().also {
                                it[index] = attr.copy(key = newKey)
                            }
                        )
                    },
                    label = { Text("Key") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = attr.value,
                    onValueChange = { newValue ->
                        onAttributesChanged(
                            attributes.toMutableList().also {
                                it[index] = attr.copy(value = newValue)
                            }
                        )
                    },
                    label = { Text("Value") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )

                var typeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                    modifier = Modifier.weight(0.7f),
                ) {
                    OutlinedTextField(
                        value = attr.type.name,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                    ) {
                        AttributeType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    onAttributesChanged(
                                        attributes.toMutableList().also {
                                            it[index] = attr.copy(type = type)
                                        }
                                    )
                                    typeExpanded = false
                                },
                            )
                        }
                    }
                }

                IconButton(onClick = {
                    onAttributesChanged(attributes.toMutableList().also { it.removeAt(index) })
                }) {
                    Text("X")
                }
            }
        }

        Button(onClick = {
            onAttributesChanged(attributes + AttributeEntry())
        }) {
            Text("Add Attribute")
        }
    }
}
