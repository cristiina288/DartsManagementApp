package org.darts.dartsmanagement.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.darts.dartsmanagement.data.bars.response.BarResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectBarBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    bars: List<BarResponse>,
    onBarSelected: (String, String) -> Unit, // barId, barName
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = ModalBackground
    ) {
        var searchQuery by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp) // Adjusted padding
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seleccionar Bar",
                    style = MaterialTheme.typography.headlineSmall.copy(color = TextPrimaryDark),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimaryDark)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nombre...", color = SearchInputPlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SearchInputPlaceholder
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = SearchInputBackground,
                    unfocusedContainerColor = SearchInputBackground,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))


            val filteredBars = bars.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }

            if (filteredBars.isEmpty()) {
                Text(
                    "No se encontraron bares.",
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = TextPrimaryDark
                )
            } else {
                LazyColumn {
                    items(filteredBars) { bar ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onBarSelected(bar.id ?: "", bar.name)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp), // Adjusted padding
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bar.name, style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimaryDark))
                        }
                    }
                }
            }
        }
    }
}

// --- Color Palette from SelectMachinesBottomSheet.kt ---
private val BackgroundDark = Color(0xFF121212)
private val ModalBackground = Color(0xFF1a2e2c) // From HTML
private val TextPrimaryDark = Color(0xFFE0E0E0)
private val SearchInputBackground = BackgroundDark
private val SearchInputPlaceholder = Color.White.copy(alpha = 0.5f)

