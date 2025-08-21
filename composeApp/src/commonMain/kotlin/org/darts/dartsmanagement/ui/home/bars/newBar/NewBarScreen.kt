package org.darts.dartsmanagement.ui.home.bars.newBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.viewmodel.koinViewModel


object NewBarScreen : Screen {
    @Composable
    override fun Content() {
        NewBarScreenContent()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBarScreenContent(
    onAddMachineClick: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var googleMapsLink by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedMachine by remember { mutableStateOf("Select Machine") }
    var selectedLocation by remember { mutableStateOf("Select Location") }
    var isDropdownExpandedMachine by remember { mutableStateOf(false) }
    var isDropdownExpandedLocation by remember { mutableStateOf(false) }
    val navigator = LocalNavigator.currentOrThrow

    val newBarViewModel = koinViewModel<NewBarViewModel>()

    val machines by newBarViewModel.machines.collectAsState()
    val locations by newBarViewModel.locations.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2832))
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navigator.pop() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Crear bar",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f)
        ) {
            // Name Field
            Text(
                text = "Nombre",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    newBarViewModel.saveName(name)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4A5568),
                    unfocusedBorderColor = Color(0xFF4A5568),
                    focusedContainerColor = Color(0xFF2D3748),
                    unfocusedContainerColor = Color(0xFF2D3748),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )

            if (locations?.isNotEmpty() == true) {
                // Select Locations Dropdown
                Text(
                    text = "Selecciona la localización",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpandedLocation,
                    onExpandedChange = { isDropdownExpandedLocation = !isDropdownExpandedLocation }
                ) {
                    OutlinedTextField(
                        value = selectedLocation,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A5568),
                            unfocusedBorderColor = Color(0xFF4A5568),
                            focusedContainerColor = Color(0xFF2D3748),
                            unfocusedContainerColor = Color(0xFF2D3748),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color.White
                            )
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpandedLocation,
                        onDismissRequest = { isDropdownExpandedLocation = false },
                        modifier = Modifier.background(Color(0xFF2D3748))
                    ) {
                        locations?.forEach { location ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = location.name ?: "",
                                        color = Color.White
                                    )
                                },
                                onClick = {
                                    selectedLocation = location.name ?: ""
                                    isDropdownExpandedLocation = false
                                    newBarViewModel.saveLocationId(location.id ?: 0)
                                }
                            )
                        }
                    }
                }
            }

            // Google Maps Link Field
            Text(
                text = "Link de Google Maps",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = googleMapsLink,
                onValueChange = {
                    googleMapsLink = it
                    newBarViewModel.saveMapLink(googleMapsLink)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4A5568),
                    unfocusedBorderColor = Color(0xFF4A5568),
                    focusedContainerColor = Color(0xFF2D3748),
                    unfocusedContainerColor = Color(0xFF2D3748),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )

            if (machines?.isNotEmpty() == true) {
                // Select Machine Dropdown
                Text(
                    text = "Selecciona la máquina del bar",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpandedMachine,
                    onExpandedChange = { isDropdownExpandedMachine = !isDropdownExpandedMachine }
                ) {
                    OutlinedTextField(
                        value = selectedMachine,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A5568),
                            unfocusedBorderColor = Color(0xFF4A5568),
                            focusedContainerColor = Color(0xFF2D3748),
                            unfocusedContainerColor = Color(0xFF2D3748),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color.White
                            )
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpandedMachine,
                        onDismissRequest = { isDropdownExpandedMachine = false },
                        modifier = Modifier.background(Color(0xFF2D3748))
                    ) {
                        machines?.forEach { machine ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = machine.name ?: "",
                                        color = Color.White
                                    )
                                },
                                onClick = {
                                    selectedMachine = machine.name ?: ""
                                    isDropdownExpandedMachine = false
                                    newBarViewModel.saveMachineId(machine.id ?: 0)
                                }
                            )
                        }
                    }
                }
            }

            // Add Machine Button
            Button(
                onClick = onAddMachineClick,
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A5568)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Añadir máquina",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            // Description Field
            Text(
                text = "Descripción",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    newBarViewModel.saveDescription(description)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4A5568),
                    unfocusedBorderColor = Color(0xFF4A5568),
                    focusedContainerColor = Color(0xFF2D3748),
                    unfocusedContainerColor = Color(0xFF2D3748),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 5
            )
        }

        // Save Changes Button
        Button(
            onClick = {
                newBarViewModel.saveBar()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0EA5E9)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Crear bar",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}