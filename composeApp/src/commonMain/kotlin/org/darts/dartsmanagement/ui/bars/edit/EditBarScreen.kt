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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import dartsmanagement.composeapp.generated.resources.Res
import dartsmanagement.composeapp.generated.resources.ico_dartboard
import dartsmanagement.composeapp.generated.resources.ico_delete
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.domain.bars.models.BarModel
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

class EditBarScreen(val bar: BarModel) : Screen {
    @Composable
    override fun Content() {
        EditBarScreenContent(bar)
    }
}

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
private fun EditBarScreenContent(bar: BarModel) {
    val editBarViewModel = koinViewModel<EditBarViewModel>(
        parameters = { parametersOf(bar) }
    )
    val allMachines by editBarViewModel.allMachines.collectAsState()

    val navigator = LocalNavigator.currentOrThrow
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var machinesInBar by remember { mutableStateOf(bar.machines) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(
                title = "Editar Bar",
                onBackClick = { navigator.pop() },
            )
        },
        bottomBar = {
            // Sticky Bottom Button
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                containerColor = Color.Transparent,
                contentPadding = PaddingValues(16.dp, 8.dp)
            ) {
                Button(
                    onClick = { /* TODO: Implement save logic */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    shape = RoundedCornerShape(12.dp),

                    ) {
                    Text(
                        text = "Guardar",
                        color = BackgroundDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showBottomSheet) {
                SelectMachinesBottomSheet(
                    sheetState = sheetState,
                    barModel = bar,
                    allMachines = allMachines,
                    selectedMachineIds = machinesInBar.mapNotNull { it.id },
                    onConfirmButton = {
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
                        val selectedMachine = allMachines.find { it.id == machineId }
                        if (selectedMachine != null) {
                            val currentMachines = machinesInBar.toMutableList()
                            if (currentMachines.any { it.id == machineId }) {
                                currentMachines.removeAll { it.id == machineId }
                            } else {
                                currentMachines.add(selectedMachine)
                            }
                            machinesInBar = currentMachines
                        }
                    }
                )
            }
            // Assigned Machines List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(machinesInBar, key = { it.id ?: 0 }) { machine ->
                    MachineEditItem(machine = machine, onDeleteClick = { machineToDelete ->
                        machinesInBar = machinesInBar.filter { it.id != machineToDelete.id }
                    })
                }

                item {
                    // Add Machine Button
                    Button(
                        onClick = {
                            showBottomSheet = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AddButtonBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Machine",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Añadir máquina",
                            color = Primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MachineEditItem(machine: MachineModel, onDeleteClick: (MachineModel) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),

        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ico_dartboard),
                    contentDescription = "Machine Icon",
                    tint = Primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Machine details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = machine.name ?: "Nombre no disponible",
                    color = TextPrimaryDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "ID: ${machine.id}",
                    color = TextSecondaryDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Delete Button
            IconButton(
                onClick = { onDeleteClick(machine) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ico_delete),
                    contentDescription = "Delete Machine",
                    tint = DeleteIconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

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
