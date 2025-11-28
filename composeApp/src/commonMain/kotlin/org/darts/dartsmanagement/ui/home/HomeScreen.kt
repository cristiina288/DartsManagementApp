package org.darts.dartsmanagement.ui.home

import ExcelExporterFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dartsmanagement.composeapp.generated.resources.Res
import dartsmanagement.composeapp.generated.resources.ico_beer
import dartsmanagement.composeapp.generated.resources.ico_dartboard
import dartsmanagement.composeapp.generated.resources.ico_documents
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.darts.dartsmanagement.ui.auth.AuthScreen
import org.darts.dartsmanagement.ui.bars.listing.BarsListingScreen
import org.darts.dartsmanagement.ui.bars.listing.BorderDark
import org.darts.dartsmanagement.ui.collections.CollectionScreen
import org.darts.dartsmanagement.ui.locations.listing.LocationsListingScreen
import org.darts.dartsmanagement.ui.machines.listing.MachinesListingScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel


object HomeScreen : Screen {
    @Composable
    override fun Content() {
        HomeScreenContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun HomeScreenContent() {
    val navigator = LocalNavigator.currentOrThrow
    val homeViewModel = koinViewModel<HomeViewModel>()
    val currentUser by homeViewModel.currentUser.collectAsState()
    val excelExporter = ExcelExporterFactory.create()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navigator.replaceAll(AuthScreen)
        }
    }

    val menuItems = listOf(
        MenuItem("Bares", icoPainter = painterResource(Res.drawable.ico_beer)) { navigator.push(BarsListingScreen) },
        MenuItem("Máquinas", icoPainter = painterResource(Res.drawable.ico_dartboard)) { navigator.push(MachinesListingScreen) },
        MenuItem("Localizaciones", Icons.Default.LocationOn) { navigator.push(LocationsListingScreen) },
        MenuItem("Historial",  icoPainter = painterResource(Res.drawable.ico_documents)) { showBottomSheet = true }
    )

    if (showBottomSheet) {
        ExportCollectionsBottomSheet(
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false },
            onExport = { selectedDate ->
                // TODO: Modify exportData to accept a date
                homeViewModel.exportData(excelExporter)
                showBottomSheet = false
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0B0F13),
        topBar = {
            AppToolbar(
                title = "Darts Management",
                onLogout = { homeViewModel.onEvent(HomeEvent.Logout) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            ActionCard(
                title = "Nueva Recaudación",
                icon = Icons.Default.AddCircle,
                onClick = { navigator.push(CollectionScreen) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp)
            ) {
                items(menuItems) { item ->
                    MenuCard(item = item)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportCollectionsBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onExport: (LocalDate) -> Unit
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    if (interactionSource.collectIsPressedAsState().value) {
        showDatePicker = true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(width = 36.dp, height = 4.dp).background(Color(0xFF3a5551), shape = RoundedCornerShape(99.dp)))
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Exportar recaudaciones",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = "Desde cuándo exportar",
                color = Color.LightGray,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = selectedDate?.let { "${it.dayOfMonth.toString().padStart(2, '0')}/${it.monthNumber.toString().padStart(2, '0')}/${it.year}" } ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Seleccionar fecha", color = Color.Gray) },
                trailingIcon = { Icon(painterResource(Res.drawable.ico_calendar), contentDescription = "Select date", tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                interactionSource = interactionSource
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { selectedDate?.let(onExport) },
                enabled = selectedDate != null,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF05b8a0))
            ) {
                Text("Exportar", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val instant = Instant.fromEpochMilliseconds(it)
                            selectedDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ActionCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BorderDark)//Color(0xFF111417))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0x1A00BFA6), RoundedCornerShape(9999.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color(0xFF00BFA6),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = title, color = Color(0xFFE6EEF3), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Comenzar", color = Color(0xFF00BFA6), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF00BFA6),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun MenuCard(item: MenuItem) {
    Card(
        modifier = Modifier
            .height(128.dp)
            .fillMaxWidth()
            .clickable(onClick = item.onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BorderDark)//Color(0xFF111417))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            if (item.icoPainter == null) {
                Icon(
                    imageVector = item.icon ?: Icons.Default.Star,
                    contentDescription = item.title,
                    tint = Color(0xFF00BFA6),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Icon(
                    painter = item.icoPainter,
                    contentDescription = item.title,
                    tint = Color(0xFF00BFA6),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = item.title, color = Color(0xFFE6EEF3), fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppToolbar(title: String, onLogout: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color(0xFFE6EEF3),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color(0xFFA0AEC0)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

data class MenuItem(val title: String, val icon: ImageVector? = null, val icoPainter: Painter? = null, val onClick: () -> Unit)