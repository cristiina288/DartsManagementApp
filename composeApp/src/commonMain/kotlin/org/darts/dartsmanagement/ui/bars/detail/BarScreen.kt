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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import dartsmanagement.composeapp.generated.resources.ico_collections
import dartsmanagement.composeapp.generated.resources.ico_dartboard
import org.darts.dartsmanagement.domain.leagues.models.LeagueModel
import org.darts.dartsmanagement.domain.leagues.models.LeagueTeamModel
import org.darts.dartsmanagement.domain.machines.model.MachineModel
import org.darts.dartsmanagement.ui.machines.detail.MachineScreen
import org.darts.dartsmanagement.ui.bars.edit.EditBarScreen
import org.darts.dartsmanagement.ui.collections.CollectionScreen
import org.darts.dartsmanagement.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class BarScreen (val barId: String) : Screen {
    @Composable
    override fun Content() {
        BarScreenContent(barId)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarScreenContent(barId: String) {
    val navigator = LocalNavigator.currentOrThrow
    val uriHandler = LocalUriHandler.current
    val viewModel = koinViewModel<BarViewModel>(parameters = { parametersOf(barId) })
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            navigator.pop()
        }
    }

    LaunchedEffect(navigator.lastItem) {
        viewModel.loadBarDetails()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            uiState.bar?.let { bar ->
                TopBar(
                    title = "Detalles del Bar",
                    onBackClick = { navigator.pop() },
                    onEditClick = { navigator.push(EditBarScreen(bar)) }
                )
            }
        },
        floatingActionButton = {
            uiState.bar?.let { bar ->
                FloatingActionButton(
                    onClick = { navigator.push(CollectionScreen(bar.id)) },
                    containerColor = PrimaryAccent,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ico_collections),
                        contentDescription = "Recaudar",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryAccent)
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
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 38.sp
                        )
                        // Address
                        Text(
                            text = bar.location.address ?: "Dirección no disponible",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )

                        // View on Map
                        if (bar.location.locationBarUrl?.isNotEmpty() == true) {
                            Text(
                                text = "Ver en Google Maps",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable { uriHandler.openUri(bar.location.locationBarUrl) }
                            )
                        } else if (bar.location.latitude != null && bar.location.longitude != null) {
                            Text(
                                text = "Ver en Google Maps",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable { uriHandler.openUri("https://www.google.com/maps/dir/?api=1&destination=${bar.location.latitude},${bar.location.longitude}") }
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Máquinas Asignadas",
                            color = TextPrimary,
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
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Ligas",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )

                        if (uiState.leagues.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                uiState.leagues.forEach { league ->
                                    LeagueItem(league = league, barId = barId)
                                }
                            }
                        } else {
                            Text(
                                text = "Sin ligas asignadas",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Notas",
                            color = TextPrimary,
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
    val status = machine.status

    val (statusText, backgroundColor, textColor) = when (status.uppercase()) {
        "ACTIVE" -> Triple("Activa", PrimaryAccent.copy(alpha = 0.2f), PrimaryAccent)
        "INACTIVE" -> Triple("Inactiva", SecondaryAccent.copy(alpha = 0.2f), SecondaryAccent)
        "PENDING_REPAIR" -> Triple("Reparación", WarmAccent.copy(alpha = 0.2f), WarmAccent)
        else -> Triple("Indefinido", Surface.copy(alpha = 0.4f), TextSecondary)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(machine) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryAccent.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, Border.copy(alpha = 0.0f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ico_dartboard),
                contentDescription = machine.name,
                tint = PrimaryAccent,
                modifier = Modifier.size(40.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = machine.name ?: "Máquina no disponible",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                Text(
                    text = "Contador: ${machine.counter ?: 0}",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Box(
                modifier = Modifier
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
private fun LeagueItem(league: LeagueModel, barId: String) {
    val leagueBar = league.bars.find { it.barId == barId } ?: return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = league.name,
                color = PrimaryAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            if (leagueBar.barFinances.paymentStatus != "N/A") {
                Spacer(modifier = Modifier.size(8.dp))
                FinanceSection(
                    title = "Finanzas del Bar",
                    total = leagueBar.barFinances.totalAmountToPay,
                    pending = leagueBar.barFinances.amountPending,
                    quota = leagueBar.barFinances.quota
                )
            }

            if (leagueBar.teams.isNotEmpty()) {
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "Equipos",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.size(4.dp))
                leagueBar.teams.forEach { team ->
                    TeamItem(team = team, priceType = league.priceType)
                }
            }
        }
    }
}

@Composable
private fun TeamItem(team: LeagueTeamModel, priceType: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(PrimaryAccent.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = team.teamName,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (priceType == "PER_PLAYER" && team.players != null) {
                Text(
                    text = "${team.players} jugadores",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        if (team.teamFinances.paymentStatus != "N/A") {
            Spacer(modifier = Modifier.size(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total: ${team.teamFinances.totalAmountToPay}€",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "Pendiente: ${team.teamFinances.amountPending}€",
                    color = if (team.teamFinances.amountPending > 0) WarmAccent else PrimaryAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FinanceSection(title: String, total: Double, pending: Double, quota: Double? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryAccent.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.size(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Total a pagar", color = TextSecondary, fontSize = 12.sp)
                Text(text = "${total}€", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Pendiente", color = TextSecondary, fontSize = 12.sp)
                Text(
                    text = "${pending}€",
                    color = if (pending > 0) WarmAccent else PrimaryAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (quota != null && quota > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Cuota", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "${quota}€", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
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
            containerColor = PrimaryAccent.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, Border.copy(alpha = 0.0f))
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            if (description.isNullOrEmpty()) {
                Text(
                    text = "Sin notas",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic
                )
            } else {
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = TextPrimary
            )
        }
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
            maxLines = 1
        )
        TextButton(onClick = onEditClick) {
            Text(
                text = "Editar",
                color = TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
