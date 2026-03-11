package org.darts.dartsmanagement.ui.machines.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.darts.dartsmanagement.domain.machines.model.MachineModel
import org.darts.dartsmanagement.ui.components.SelectBarBottomSheet
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf


data class EditMachineScreen(val machine: MachineModel) : Screen {
    @Composable
    override fun Content() {
        EditMachineScreenContent(machine)
    }
}

private val BackgroundDark = Color(0xFF121212)
private val Primary = Color(0xFF00BFA6)
private val DialogCardBackground = Color(0xFF1C1C1E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMachineScreenContent(machine: MachineModel) {
    val navigator = LocalNavigator.currentOrThrow
    val editMachineViewModel = koinViewModel<EditMachineViewModel>(parameters = { parametersOf(machine) })
    val uiState by editMachineViewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        editMachineViewModel.effect.collect { effect ->
            when (effect) {
                EditMachineViewModel.Effect.MachineSaved -> navigator.pop()
                EditMachineViewModel.Effect.MachineDeleted -> {
                    navigator.pop() // Pop Edit Screen
                    navigator.pop() // Pop Detail Screen
                }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(errorMessage)
            editMachineViewModel.onEvent(EditMachineEvent.ClearError)
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                editMachineViewModel.onEvent(EditMachineEvent.OnDeleteMachineClick)
                showDeleteDialog = false
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Editar Máquina",
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
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Add some space after TopAppBar

            // Serial Number Field
            OutlinedTextField(
                value = uiState.serialNumber,
                onValueChange = { },
                label = { Text("Número de Serie") },
                placeholder = { Text("Introduce el número de serie de la máquina") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                shape = RoundedCornerShape(8.dp),
                enabled = false
            )

            // Name Field
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { editMachineViewModel.onEvent(EditMachineEvent.OnNameChange(it)) },
                label = { Text("Nombre de la máquina") },
                placeholder = { Text("Introduce el nombre de la máquina") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )

            // Counter Field
            OutlinedTextField(
                value = uiState.counter,
                onValueChange = { editMachineViewModel.onEvent(EditMachineEvent.OnCounterChange(it)) },
                label = { Text("Contador actual") },
                placeholder = { Text("Introduce el contador actual") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )

            // Select Bar
            Button(
                onClick = { editMachineViewModel.onEvent(EditMachineEvent.OnBarSelectionClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Similar height to OutlinedTextField
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f) // Match TextField's container color
                ),
                shape = RoundedCornerShape(8.dp), // Match TextField's shape
                contentPadding = PaddingValues(horizontal = 16.dp), // Match TextField's horizontal padding
                // Add border if needed, similar to OutlinedTextField
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)) // Match TextField's unfocused border color
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = uiState.selectedBarName ?: "Seleccionar bar",
                        color = Color.White,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )

                    if (!uiState.selectedBarId.isNullOrEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Quitar Bar",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable {
                                    editMachineViewModel.onEvent(EditMachineEvent.OnClearBarSelection)
                                }
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Seleccionar Bar",
                        tint = Color.White
                    )
                }
            }

            // Save Changes Button
            Button(
                onClick = { editMachineViewModel.onEvent(EditMachineEvent.OnSaveMachineClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BFA6)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = "Guardar Cambios",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (uiState.showBarSelectionDialog) {
            SelectBarBottomSheet(
                sheetState = sheetState,
                bars = uiState.allBars,
                onBarSelected = { barId, barName ->
                    editMachineViewModel.onEvent(EditMachineEvent.OnBarSelected(barId, barName))
                },
                onDismiss = {
                    editMachineViewModel.onEvent(EditMachineEvent.OnDismissBarSelectionDialog)
                }
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

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = DialogCardBackground)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "¿Estás seguro de que deseas eliminar esta máquina? Se desvinculará del bar si está asignada.",
                    color = Color(0xFFF2F2F7),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    Text(
                        text = "Sí",
                        color = Color(0xFF101817),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Primary
                    )
                ) {
                    Text(
                        text = "Cancelar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
