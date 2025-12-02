package org.darts.dartsmanagement.ui.bars.newBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.ui.bars.components.AssignedMachinesSection
import org.darts.dartsmanagement.ui.bars.edit.SelectMachinesBottomSheet
import org.koin.compose.viewmodel.koinViewModel

object NewBarScreen : Screen {
    @Composable
    override fun Content() {
        NewBarScreenContent()
    }
}

val BackgroundDark = Color(0xFF121212)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBarScreenContent() {
    val navigator = LocalNavigator.currentOrThrow
    val newBarViewModel = koinViewModel<NewBarViewModel>()
    val uiState by newBarViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navigator.pop()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crear Bar",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0F13).copy(alpha = 0.8f)
                ),
                actions = {
                    Spacer(modifier = Modifier.width(48.dp)) // To balance the title
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFF0B0F13).copy(alpha = 0.8f)
            ) {
                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Button(
                    onClick = { newBarViewModel.onEvent(NewBarEvent.OnSaveTapped) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = "Crear Bar",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (showBottomSheet) {
                val filteredMachines = uiState.allMachines.filter { it.barId.isNullOrEmpty() }
                SelectMachinesBottomSheet(
                    sheetState = sheetState,
                    allMachines = filteredMachines,
                    selectedMachineIds = uiState.selectedMachineIds.toList(),
                    onConfirmButton = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    },
                    onDismissRequest = { showBottomSheet = false },
                    onToggleMachineSelection = { machineId ->
                        newBarViewModel.onEvent(NewBarEvent.ToggleMachineSelection(machineId))
                    }
                )
            }

            BarDetailsSection(
                name = uiState.name,
                onNameChange = { newBarViewModel.onEvent(NewBarEvent.OnNameChanged(it)) },
                description = uiState.description,
                onDescriptionChange = { newBarViewModel.onEvent(NewBarEvent.OnDescriptionChanged(it)) }
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            LocationSection(
                uiState = uiState,
                locations = uiState.locations,
                selectedLocation = uiState.selectedLocation,
                onLocationSelected = { locationId ->
                    newBarViewModel.onEvent(NewBarEvent.OnLocationSelected(locationId))
                },
                onAddressChange = { newBarViewModel.onEvent(NewBarEvent.OnAddressChanged(it)) },
                onLatitudeChange = { newBarViewModel.onEvent(NewBarEvent.OnLatitudeChanged(it)) },
                onLongitudeChange = { newBarViewModel.onEvent(NewBarEvent.OnLongitudeChanged(it)) },
                onLocationBarUrlChange = {
                    newBarViewModel.onEvent(
                        NewBarEvent.OnLocationBarUrlChanged(
                            it
                        )
                    )
                }
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            AssignedMachinesSection(
                machines = uiState.assignedMachines,
                onAddMachineClick = { showBottomSheet = true },
                onDeleteMachineClick = { machine ->
                    machine.id?.let {
                        newBarViewModel.onEvent(NewBarEvent.ToggleMachineSelection(it))
                    }
                }
            )
        }
    }
}

@Composable
private fun BarDetailsSection(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre del bar") },
            placeholder = { Text("Introduce el nombre del bar") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors(),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Descripción") },
            placeholder = { Text("Introduce una descripción") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = textFieldColors(),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSection(
    uiState: NewBarUiState,
    locations: List<org.darts.dartsmanagement.domain.locations.model.LocationModel>,
    selectedLocation: org.darts.dartsmanagement.domain.locations.model.LocationModel?,
    onLocationSelected: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onLocationBarUrlChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Localización",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedLocation?.name ?: "Seleccionar población",
                onValueChange = {},
                readOnly = true,
                label = { Text("Población") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = textFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF15181B))
            ) {
                locations.forEach { location ->
                    DropdownMenuItem(
                        text = { Text(location.name ?: "", color = Color.White) },
                        onClick = {
                            location.id?.let { onLocationSelected(it) }
                            expanded = false
                        }
                    )
                }
            }
        }

        selectedLocation?.let {
            OutlinedTextField(
                value = uiState.address,
                onValueChange = { onAddressChange(it) },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.latitude,
                    onValueChange = { onLatitudeChange(it) },
                    label = { Text("Latitud") },
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = uiState.longitude,
                    onValueChange = { onLongitudeChange(it) },
                    label = { Text("Longitud") },
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            OutlinedTextField(
                value = uiState.locationBarUrl,
                onValueChange = { onLocationBarUrlChange(it) },
                label = { Text("URL de Google Maps (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value ?: "N/A",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}


@Composable
private fun textFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF00BFA6).copy(alpha = 0.5f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    focusedLabelColor = Color.White.copy(alpha = 0.8f),
    unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color.White.copy(alpha = 0.1f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
    cursorColor = Color(0xFF00BFA6),
    focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
)