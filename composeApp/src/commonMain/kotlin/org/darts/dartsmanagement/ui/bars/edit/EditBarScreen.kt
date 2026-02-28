package org.darts.dartsmanagement.ui.bars.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.darts.dartsmanagement.ui.bars.components.AssignedMachinesSection
import org.darts.dartsmanagement.ui.bars.components.MachineItem
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.locations.model.LocationModel
import org.darts.dartsmanagement.domain.machines.model.MachineModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf


// --- Color Palette from BarScreen.kt ---
private val BackgroundDark = Color(0xFF121212)
private val SurfaceDark = Color(0xFF1E1E1E)
private val Primary = Color(0xFF00BDA4)
private val TextPrimaryDark = Color(0xFFE0E0E0)
private val TextSecondaryDark = Color(0xFFB0B0B0)
private val BorderDark = Color.White.copy(alpha = 0.1f)

// New colors from HTML design
private val CardBackground = Color(0xFF1a2e2c)
private val AddButtonBackground = Color(0xFF2b4d49)
private val DeleteIconColor = Color(0xFFf87171)

class EditBarScreen(val initialBar: BarModel) : Screen {
    @Composable
    override fun Content() {
        EditBarScreenContent(initialBar)
    }
}

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
private fun EditBarScreenContent(initialBar: BarModel) {
    val editBarViewModel = koinViewModel<EditBarViewModel>(
        parameters = { parametersOf(initialBar) }
    )
    val uiState by editBarViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val navigator = LocalNavigator.currentOrThrow
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filteredMachines = uiState.allMachines.filter {
        it.barId == uiState.bar?.id.toString() || it.barId.isNullOrEmpty()
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navigator.pop()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(
                title = "Editar Bar",
                onBackClick = { navigator.pop() },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                if (uiState.isSaving) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                BottomAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    containerColor = Color.Transparent,
                    contentPadding = PaddingValues(16.dp, 8.dp)
                ) {
                    Button(
                        onClick = { editBarViewModel.onEvent(EditBarEvent.OnSaveTapped) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isSaving
                    ) {
                        Text(
                            text = if (uiState.isSaving) "Guardando..." else "Guardar",
                            color = BackgroundDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
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
                SelectMachinesBottomSheet(
                    sheetState = sheetState,
                    allMachines = filteredMachines,
                    selectedMachineIds = uiState.selectedMachineIds.toList(),
                    onConfirmButton = { selectedIds ->
                        // The ViewModel's uiState.selectedMachineIds is already updated by ToggleMachineSelection
                        // No need to pass selectedIds back here to ViewModel for updateBarMachines
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    },
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    onToggleMachineSelection = { machineId ->
                        editBarViewModel.onEvent(EditBarEvent.ToggleMachineSelection(machineId))
                    }
                )
            }

            BarDetailsSection(
                name = uiState.name,
                onNameChange = { editBarViewModel.onEvent(EditBarEvent.OnNameChanged(it)) },
                description = uiState.description,
                onDescriptionChange = { editBarViewModel.onEvent(EditBarEvent.OnDescriptionChanged(it)) }
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            LocationSection(
                uiState = uiState,
                locations = uiState.locations,
                selectedLocation = uiState.selectedLocation,
                onLocationSelected = { locationId ->
                    editBarViewModel.onEvent(EditBarEvent.OnLocationSelected(locationId))
                },
                onAddressChange = { editBarViewModel.onEvent(EditBarEvent.OnAddressChanged(it)) },
                onLatitudeChange = { editBarViewModel.onEvent(EditBarEvent.OnLatitudeChanged(it)) },
                onLongitudeChange = { editBarViewModel.onEvent(EditBarEvent.OnLongitudeChanged(it)) },
                onLocationBarUrlChange = {
                    editBarViewModel.onEvent(EditBarEvent.OnLocationBarUrlChanged(it))
                }
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Assigned Machines List
            AssignedMachinesSection(
                machines = uiState.bar?.machines ?: emptyList(),
                onAddMachineClick = { showBottomSheet = true },
                onDeleteMachineClick = { machineToDelete ->
                    editBarViewModel.onEvent(EditBarEvent.RemoveMachineFromBar(machineToDelete.id ?: 0))
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
    uiState: EditBarUiState,
    locations: List<LocationModel>,
    selectedLocation: LocationModel?,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = TextPrimaryDark
            )
        }
        Text(
            text = title,
            color = TextPrimaryDark,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}
