package org.darts.dartsmanagement.ui.machines.detail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.domain.machines.model.MachineModel

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.darts.dartsmanagement.ui.bars.detail.BarScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import dartsmanagement.composeapp.generated.resources.Res
import dartsmanagement.composeapp.generated.resources.ico_collections
import org.darts.dartsmanagement.domain.common.models.Status
import org.darts.dartsmanagement.domain.common.models.toStatus
import org.jetbrains.compose.resources.painterResource

// --- Color Palette from BarScreen.kt ---
private val BackgroundDark = Color(0xFF121212)
private val SurfaceDark = Color(0xFF1E1E1E)
private val Primary = Color(0xFF00BDA4)
private val TextPrimaryDark = Color(0xFFE0E0E0)
private val TextSecondaryDark = Color(0xFFB0B0B0)
private val BorderDark = Color.White.copy(alpha = 0.1f)

private val InactiveStatusColor = Color(0xFF8BE9FD) // Secondary Accent
private val PendingRepairStatusColor = Color(0xFFFFB86B) // Warm Accent
private val DialogCardBackground = Color(0xFF1C1C1E)

class MachineScreen(val machine: MachineModel) : Screen {
    @Composable
    override fun Content() {
        MachineScreenContent(machine)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineScreenContent(
    machine: MachineModel
) {
    val navigator = LocalNavigator.currentOrThrow
    var showRepairDialog by remember { mutableStateOf(false) }

    val machineViewModel = koinViewModel<MachineViewModel>(
        parameters = { parametersOf(machine) }
    )
    val uiState by machineViewModel.uiState.collectAsState()
    val currentMachine = uiState.machine ?: machine

    val text = if (currentMachine.status.id == Status.PENDING_REPAIR.id) {
        "¿Ya tienes la máquina reparada y la quieres marcar como 'inactiva'?"
    } else {
        "¿Enviar máquina a 'reparación'?"
    }

    if (showRepairDialog) {
        RepairConfirmationDialog(
            text = text,
            onConfirm = {
                machineViewModel.onEvent(MachineEvent.ToggleRepairStatus)
                showRepairDialog = false
            },
            onDismiss = {
                showRepairDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navigator.pop() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimaryDark
                )
            }

            Text(
                text = "Machine Details",
                color = TextPrimaryDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            if (currentMachine.status.id == Status.INACTIVE.id || currentMachine.status.id == Status.PENDING_REPAIR.id) {
                TextButton(
                    onClick = { showRepairDialog = true }
                ) {
                    Text(
                        text = "Edit",
                        color = Primary,
                        fontSize = 16.sp
                    )
                }
            }
        }
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Machine Info Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Machine Name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentMachine.name.orEmpty(),
                            color = TextPrimaryDark,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 38.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        MachineStatusTag(status =
                            currentMachine.status.toStatus)
                    }


                    // "Situada en [Bar Name]" link
                    uiState.bar?.let { currentBar ->
                        Text(
                            text = "Situada en ${currentBar.name}",
                            color = TextSecondaryDark,
                            fontSize = 16.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable {
                                    navigator.push(BarScreen(currentBar.id ?: ""))
                                }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Collection History Title
                Text(
                    text = "Historial de Recaudaciones",
                    color = TextPrimaryDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Collection History Items
            itemsIndexed(uiState.collections) { index, collection ->
                CollectionHistoryItem(
                    collection = collection,
                    item = index
                )
            }
        }
    }
}

@Composable
fun MachineStatusTag(status: Status) {
    val (statusText, backgroundColor, textColor) = when (status) {
        Status.UNDEFINED -> Triple("Indefinido", SurfaceDark.copy(alpha = 0.4f), TextSecondaryDark)
        Status.ACTIVE -> Triple("Activa", Primary.copy(alpha = 0.2f), Primary)
        Status.INACTIVE -> Triple("Inactiva", InactiveStatusColor.copy(alpha = 0.2f), InactiveStatusColor)
        Status.PENDING_REPAIR -> Triple("Reparación", PendingRepairStatusColor.copy(alpha = 0.2f), PendingRepairStatusColor)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(backgroundColor, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = statusText,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun RepairConfirmationDialog(
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DialogCardBackground)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = text,
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


@Composable
fun CollectionHistoryItem(
    collection: CollectionModel,
    item: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.1f)
        ),
        //border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) // Optional: Add a subtle border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ico_collections),
                contentDescription = "Collection Icon",
                tint = Primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${collection.totalCollection ?: 0.0} €",
                    color = TextPrimaryDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                val localDateTime = Instant.fromEpochSeconds(collection.createdAt)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                val formattedDate =
                    "${localDateTime.dayOfMonth}/${localDateTime.monthNumber}/${localDateTime.year} ${localDateTime.hour}:${localDateTime.minute}"

                Text(
                    text = "Fecha: $formattedDate",
                    color = TextSecondaryDark,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}