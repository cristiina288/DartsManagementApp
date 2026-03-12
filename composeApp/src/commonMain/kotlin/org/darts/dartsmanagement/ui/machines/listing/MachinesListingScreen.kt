package org.darts.dartsmanagement.ui.machines.listing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dartsmanagement.composeapp.generated.resources.Res
import dartsmanagement.composeapp.generated.resources.ico_dartboard
import org.darts.dartsmanagement.domain.machines.model.MachineModel
import org.darts.dartsmanagement.ui.machines.detail.MachineScreen
import org.darts.dartsmanagement.ui.machines.newMachine.NewMachineScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import org.darts.dartsmanagement.ui.machines.listing.MachinesUiModel
import org.darts.dartsmanagement.ui.theme.*

object MachinesListingScreen : Screen {
    @Composable
    override fun Content() {
        MachinesListingScreenContent()
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
private fun MachinesListingScreenContent() {
    val machinesListingViewModel = koinViewModel<MachinesListingViewModel>()
    val machinesUiModel by machinesListingViewModel.machinesUiModel.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(navigator.lastItem) {
        machinesListingViewModel.loadMachines()
    }

    Scaffold(
        containerColor = Background,
        topBar = { TopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigator.push(NewMachineScreen) },
                containerColor = PrimaryAccent,
                contentColor = Background,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar máquina",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MachineSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val filteredMachines = machinesUiModel.filter {
                    it.machine.name?.contains(searchQuery, ignoreCase = true) == true ||
                            it.barName?.contains(searchQuery, ignoreCase = true) == true
                } ?: emptyList()

                items(filteredMachines, key = { it.machine.id?.toString() ?: "" }) { machinesUiModel ->
                    MachineListItem(machineDisplayModel = machinesUiModel, onClick = {
                        navigator.push(MachineScreen(machinesUiModel.machine))
                    })
                }
            }
        }
    }
}

@Composable
private fun MachineSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Buscar máquina...", color = TextSecondary) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            disabledContainerColor = Surface,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun MachineListItem(machineDisplayModel: MachinesUiModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        border = BorderStroke(1.dp, Border)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ico_dartboard),
                contentDescription = machineDisplayModel.machine.name,
                tint = PrimaryAccent,
                modifier = Modifier
                    .size(48.dp)
                    .padding(start = 16.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = machineDisplayModel.machine.name ?: "Nombre no disponible",
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                if (machineDisplayModel.barName.isNullOrEmpty()) {
                    Text(
                        text = "Sin bar asignado",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        fontSize = 14.sp,
                        maxLines = 1,
                    )
                } else {
                    Text(
                        text = machineDisplayModel.barName,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            val status = machineDisplayModel.machine.status

            val (statusText, backgroundColor, textColor) = when (status.uppercase()) {
                "ACTIVE" -> Triple("Activa", PrimaryAccent.copy(alpha = 0.2f), PrimaryAccent)
                "INACTIVE" -> Triple("Inactiva", SecondaryAccent.copy(alpha = 0.2f), SecondaryAccent)
                "PENDING_REPAIR" -> Triple("Reparación", WarmAccent.copy(alpha = 0.2f), WarmAccent)
                else -> Triple("Indefinido", Surface.copy(alpha = 0.4f), TextSecondary)
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(end = 16.dp)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    val navigator = LocalNavigator.currentOrThrow

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navigator.pop() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = TextPrimary
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Máquinas",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}