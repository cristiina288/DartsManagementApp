package org.darts.dartsmanagement.ui.bars.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dartsmanagement.composeapp.generated.resources.Res
import dartsmanagement.composeapp.generated.resources.ico_dartboard
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.common.models.Status
import org.darts.dartsmanagement.domain.common.models.toStatus
import org.darts.dartsmanagement.domain.machines.model.MachineModel
import org.darts.dartsmanagement.ui.machines.detail.MachineScreen
import org.darts.dartsmanagement.ui.bars.edit.EditBarScreen
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// --- Color Palette (from MachinesListingScreen.kt) ---
private val BackgroundDark = Color(0xFF121212)
private val SurfaceDark = Color(0xFF1E1E1E)
private val Primary = Color(0xFF00BDA4)
private val TextPrimaryDark = Color(0xFFE0E0E0)
private val TextSecondaryDark = Color(0xFFB0B0B0)
private val BorderDark = Color.White.copy(alpha = 0.1f)

// Specific tones for status tags (from GEMINI.md)
private val InactiveStatusColor = Color(0xFF8BE9FD) // Secondary Accent
private val PendingRepairStatusColor = Color(0xFFFFB86B) // Warm Accent

class BarScreen (val barId: String) : Screen {
    @Composable
    override fun Content() {
        BarScreenContent(barId)
    }
}

@Composable
private fun BarScreenContent(barId: String) {
    val navigator = LocalNavigator.currentOrThrow
    val uriHandler = LocalUriHandler.current
    val viewModel = koinViewModel<BarViewModel>(parameters = { parametersOf(barId) })
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(navigator.lastItem) {
        viewModel.loadBarDetails()
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            uiState.bar?.let { bar ->
                TopBar(
                    title = "Detalles del Bar",
                    onBackClick = { navigator.pop() },
                    onEditClick = { navigator.push(EditBarScreen(bar)) }
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            uiState.bar?.let { bar ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        // Bar Title
                        Text(
                            text = bar.name,
                            color = TextPrimaryDark,
                            fontSize = 32.sp, // Matching HTML
                            fontWeight = FontWeight.Bold,
                            lineHeight = 38.sp
                        )
                        // Address
                        Text(
                            text = bar.location.address ?: "Dirección no disponible",
                            color = TextSecondaryDark,
                            fontSize = 16.sp
                        )

                        // View on Map
                        if (bar.location.locationBarUrl?.isNotEmpty() == true) {
                            Text(
                                text = "Ver en Google Maps",
                                color = TextSecondaryDark,
                                fontSize = 14.sp,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable { uriHandler.openUri(bar.location.locationBarUrl) }
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Máquinas Asignadas",
                            color = TextPrimaryDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )

                        if (bar.machines.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                bar.machines.forEach { machine ->
                                    MachineAssignedItem(machine = machine, onClick = { clickedMachine ->
                                        navigator.push(MachineScreen(clickedMachine))
                                    })
                                }
                            }
                        } else {
                            Text(
                                text = "Sin máquinas asignadas",
                                color = TextSecondaryDark,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Notas",
                            color = TextPrimaryDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        NotesSection(bar.description)
                    }
                }
            }
        }
    }
}

@Composable
private fun MachineAssignedItem(machine: MachineModel, onClick: (MachineModel) -> Unit) {
    val status = machine.status.toStatus // Use the extension property

    val (statusText, backgroundColor, textColor) = when (status) {
        Status.UNDEFINED -> Triple("Indefinido", SurfaceDark.copy(alpha = 0.4f), TextSecondaryDark)
        Status.ACTIVE -> Triple("Activa", Primary.copy(alpha = 0.2f), Primary)
        Status.INACTIVE -> Triple("Inactiva", InactiveStatusColor.copy(alpha = 0.2f), InactiveStatusColor)
        Status.PENDING_REPAIR -> Triple("Reparación", PendingRepairStatusColor.copy(alpha = 0.2f), PendingRepairStatusColor)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(machine) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, BorderDark.copy(alpha = 0.0f)) // No visible border for machine items
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                painter = painterResource(Res.drawable.ico_dartboard),
                contentDescription = machine.name,
                tint = Primary,
                modifier = Modifier.size(40.dp)
            )


            Spacer(Modifier.width(16.dp))

            // Machine details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = machine.name ?: "Máquina no disponible",
                    color = TextPrimaryDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                // Counter/Amount
                Text(
                    text = "Contador: ${machine.counter ?: 0}",
                    color = TextPrimaryDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                )
            }

            // Status Tag
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .background(backgroundColor, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = statusText,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun NotesSection(description: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, BorderDark.copy(alpha = 0.0f))
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            if (description.isNullOrEmpty()) {
                Text(
                    text = "Sin notas",
                    color = TextSecondaryDark,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic
                )
            } else {
                Text(
                    text = description,
                    color = TextSecondaryDark,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(title: String, onBackClick: () -> Unit, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark) // Use same BackgroundDark as screen
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
            fontSize = 22.sp, // Matching other screens
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onEditClick) {
            Text(
                text = "Editar",
                color = Primary, // From HTML
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
