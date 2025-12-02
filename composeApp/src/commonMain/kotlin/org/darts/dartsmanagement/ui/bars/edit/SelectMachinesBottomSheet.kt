package org.darts.dartsmanagement.ui.bars.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.machines.model.MachineModel
import org.darts.dartsmanagement.domain.common.model.StatusModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// --- Color Palette from BarScreen.kt ---
private val BackgroundDark = Color(0xFF121212)
private val SurfaceDark = Color(0xFF1E1E1E)
private val Primary = Color(0xFF00BDA4)
private val TextPrimaryDark = Color(0xFFE0E0E0)
private val TextSecondaryDark = Color(0xFFB0B0B0)
private val CardBackground = Color(0xFF1a2e2c) // From HTML

// New colors from HTML design
private val ModalBackground = CardBackground // Reusing for consistency
private val ModalBorder = Color.White.copy(alpha = 0.1f) // From HTML border-white/10
private val CloseButtonTint = Color.White.copy(alpha = 0.7f) // From HTML text-white/70
private val SearchInputBackground = BackgroundDark // From HTML bg-background-dark
private val SearchInputPlaceholder = Color.White.copy(alpha = 0.5f) // From HTML placeholder:text-white/50
private val HoverColor = Color.White.copy(alpha = 0.05f) // From HTML hover:bg-white/5
private val CheckboxBorder = Color.White.copy(alpha = 0.3f) // From HTML border-white/30
private val ConfirmButtonShadow = Primary.copy(alpha = 0.2f) // From HTML shadow-primary/20


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectMachinesBottomSheet(
    sheetState: SheetState,
    allMachines: List<MachineModel>,
    selectedMachineIds: List<Int>,
    onConfirmButton: (List<Int>) -> Unit,
    onDismissRequest: () -> Unit,
    onToggleMachineSelection: (Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = ModalBackground
    ) {
        SelectMachinesBottomSheetContent(
            allMachines = allMachines,
            selectedMachineIds = selectedMachineIds,
            onConfirm = onConfirmButton,
            onDismiss = onDismissRequest,
            onSearchQueryChange = { /* TODO: Implement search logic in ViewModel */ },
            onToggleMachineSelection = { machineId ->
                onToggleMachineSelection(machineId)//selectMachinesViewModel.toggleMachineSelection(machineId)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectMachinesBottomSheetContent(
    allMachines: List<MachineModel>,
    selectedMachineIds: List<Int>,
    onConfirm: (List<Int>) -> Unit,
    onDismiss: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleMachineSelection: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Seleccionar máquinas",
                color = TextPrimaryDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearchQueryChange(it)
                },
                placeholder = { Text("Buscar por nombre o ID...", color = SearchInputPlaceholder) },
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
        }


        // Machine List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 16.dp)
                .background(ModalBackground),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val filteredMachines = allMachines.filter {
                it.name?.contains(searchQuery, ignoreCase = true) == true ||
                        it.id.toString().contains(searchQuery, ignoreCase = true) == true
            }

            items(filteredMachines, key = { it.id ?: 0 }) { machine ->
                val isSelected = selectedMachineIds.contains(machine.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) HoverColor else Color.Transparent)
                        .clickable {
                            if (machine.id != null) {
                                onToggleMachineSelection(machine.id)
                            }
                        }
                        .padding(12.dp), // p-3
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = machine.name ?: "Máquina sin nombre",
                            color = TextPrimaryDark, // text-white
                            fontSize = 16.sp, // text-base
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "ID: ${machine.id}",
                            color = TextSecondaryDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { _ ->
                            if (machine.id != null) {
                                onToggleMachineSelection(machine.id)
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Primary,
                            uncheckedColor = CheckboxBorder,
                            disabledCheckedColor = Primary,
                            disabledUncheckedColor = CheckboxBorder
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Confirmation Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ModalBackground)
                .padding(16.dp)
                .padding(bottom = 42.dp)
        ) {
            Button(
                onClick = {
                    onConfirm(selectedMachineIds)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = "Confirmar selección",
                    color = BackgroundDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
