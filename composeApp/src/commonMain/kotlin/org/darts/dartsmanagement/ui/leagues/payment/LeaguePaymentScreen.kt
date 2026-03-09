package org.darts.dartsmanagement.ui.leagues.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.darts.dartsmanagement.domain.bars.models.BarModel

// --- Reuse color palette and basic components structure from CollectionsScreen ---
private val BackgroundDark = Color(0xFF0B0F13)
private val SurfaceDark = Color.White.copy(alpha = 0.1f)
private val InputBackground = Color(0xFF273A38)
private val Primary = Color(0xFF00BDA4)
private val TextPrimaryDark = Color.White
private val TextSecondaryDark = Color.White.copy(alpha = 0.8f)
private val TextPlaceholder = Color.White.copy(alpha = 0.5f)

object LeaguePaymentScreen : Screen {
    @Composable
    override fun Content() {
        LeaguePaymentScreenContent()
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun LeaguePaymentScreenContent() {
    val viewModel = koinViewModel<LeaguePaymentViewModel>()
    val state by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Pago guardado correctamente")
                navigator.pop()
            }
        }
    }

    val isSaveButtonEnabled = state.selectedLeague != null && 
        (if (state.selectedLeague?.ownerPayment == "BAR") state.selectedBar != null else state.selectedTeam != null) &&
        state.amount.isNotEmpty() && (state.amount.toDoubleOrNull() ?: 0.0) > 0 && state.error == null

    Scaffold(
        containerColor = BackgroundDark,
        topBar = { TopBar(onBack = { navigator.pop() }) },
        bottomBar = {
            BottomBar(
                onSave = { viewModel.onEvent(LeaguePaymentEvent.OnSavePayment) },
                enabled = isSaveButtonEnabled && !state.isSaving
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Section(title = "Liga") {
                    AppDropdown(
                        label = "Liga",
                        options = state.leagues,
                        selectedOption = state.selectedLeague,
                        onOptionSelected = { viewModel.onEvent(LeaguePaymentEvent.OnLeagueSelected(it)) },
                        optionToString = { it.name }
                    )
                }
            }

            if (state.selectedLeague != null) {
                val ownerPayment = state.selectedLeague?.ownerPayment

                item {
                    Section(title = "Detalles del Pago") {
                        if (ownerPayment == "TEAM") {
                            Text(
                                text = "Buscar por:",
                                color = TextSecondaryDark,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            SegmentedToggle(
                                options = listOf("Bar", "Equipo"),
                                selectedIndex = if (state.searchMode == SearchMode.BAR) 0 else 1,
                                onOptionSelected = { idx ->
                                    val mode = if (idx == 0) SearchMode.BAR else SearchMode.TEAM
                                    viewModel.onEvent(LeaguePaymentEvent.OnSearchModeChanged(mode))
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.searchMode == SearchMode.BAR) {
                                SearchableAppSuggestedDropdown(
                                    label = "Bar",
                                    options = state.barsInLeague,
                                    selectedOption = state.selectedBar,
                                    onOptionSelected = { viewModel.onEvent(LeaguePaymentEvent.OnBarSelected(it)) },
                                    optionToString = { it.name },
                                    searchQuery = state.barSearchQuery,
                                    onSearchQueryChanged = { viewModel.onEvent(LeaguePaymentEvent.OnBarSearchQueryChanged(it)) }
                                )

                                if (state.selectedBar != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    AppDropdown(
                                        label = "Equipo",
                                        options = state.teamsForSelectedBar,
                                        selectedOption = state.selectedTeam,
                                        onOptionSelected = { viewModel.onEvent(LeaguePaymentEvent.OnTeamSelected(it)) },
                                        optionToString = { it.teamName }
                                    )
                                }
                            } else {
                                SearchableAppSuggestedDropdown(
                                    label = "Equipo",
                                    options = state.teamsInLeague,
                                    selectedOption = state.selectedTeam,
                                    onOptionSelected = { viewModel.onEvent(LeaguePaymentEvent.OnTeamSelected(it)) },
                                    optionToString = { "${it.teamName} (${it.barName ?: "Sin bar"})" },
                                    searchQuery = state.teamSearchQuery,
                                    onSearchQueryChanged = { viewModel.onEvent(LeaguePaymentEvent.OnTeamSearchQueryChanged(it)) }
                                )
                            }
                        } else {
                            // ownerPayment == BAR
                            SearchableAppSuggestedDropdown(
                                label = "Bar",
                                options = state.barsInLeague,
                                selectedOption = state.selectedBar,
                                onOptionSelected = { viewModel.onEvent(LeaguePaymentEvent.OnBarSelected(it)) },
                                optionToString = { it.name },
                                searchQuery = state.barSearchQuery,
                                onSearchQueryChanged = { viewModel.onEvent(LeaguePaymentEvent.OnBarSearchQueryChanged(it)) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        AppTextField(
                            label = "Precio (€)",
                            value = state.amount,
                            onValueChange = { viewModel.onEvent(LeaguePaymentEvent.OnAmountChanged(it)) },
                            placeholder = "0.00",
                            isError = state.error != null,
                            helperText = state.error,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = { Text("€", color = TextPlaceholder) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val paymentMethods = listOf("CASH", "TRANSFER", "PAYPAL", "BIZUM", "CASH_COLLECTION")
                        AppDropdown(
                            label = "Método de Pago",
                            options = paymentMethods,
                            selectedOption = state.paymentMethod,
                            onOptionSelected = { viewModel.onEvent(LeaguePaymentEvent.OnPaymentMethodChanged(it)) },
                            optionToString = { 
                                when(it) {
                                    "CASH" -> "Efectivo"
                                    "TRANSFER" -> "Transferencia"
                                    "PAYPAL" -> "PayPal"
                                    "BIZUM" -> "Bizum"
                                    "CASH_COLLECTION" -> "Recaudación"
                                    else -> it
                                }
                            }
                        )
                    }
                }
            }
        }

        if (state.isSaving || state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        }
    }
}

// --- Internal Components (Reusing style from CollectionsScreen) ---

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = title, color = TextPrimaryDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> AppDropdown(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionToString: (T) -> String,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(
            text = label,
            color = TextSecondaryDark,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded },
        ) {
            OutlinedTextField(
                value = selectedOption?.let { optionToString(it) } ?: "Seleccionar",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = InputBackground,
                    focusedContainerColor = InputBackground,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedTextColor = TextPrimaryDark,
                    focusedTextColor = TextPrimaryDark
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = TextPlaceholder) },
                enabled = enabled
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(SurfaceDark)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionToString(option), color = TextPrimaryDark) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SearchableAppSuggestedDropdown(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    optionToString: (T) -> String,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    val filteredOptions = if (searchQuery.isEmpty()) {
        emptyList()
    } else {
        options.filter { optionToString(it).contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(filteredOptions) {
        expanded = filteredOptions.isNotEmpty()
    }

    Column {
        Text(
            text = label,
            color = TextSecondaryDark,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = searchQuery.ifEmpty { selectedOption?.let { optionToString(it) } ?: "" },
                onValueChange = {
                    onSearchQueryChanged(it)
                    if (it.isEmpty()) onOptionSelected(null)
                },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                placeholder = { Text("Escribe para buscar...", color = TextPlaceholder) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = InputBackground,
                    focusedContainerColor = InputBackground,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedTextColor = TextPrimaryDark,
                    focusedTextColor = TextPrimaryDark
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = enabled
            )
            
            if (expanded) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    filteredOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(optionToString(option), color = TextPrimaryDark) },
                            onClick = {
                                onOptionSelected(option)
                                onSearchQueryChanged("")
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    isError: Boolean = false,
    helperText: String? = null,
    containerColor: Color = InputBackground,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                color = if (isError) Color.Red.copy(alpha = 0.8f) else TextSecondaryDark,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextPlaceholder) },
            readOnly = readOnly,
            singleLine = singleLine,
            enabled = enabled,
            isError = isError,
            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimaryDark, fontSize = 16.sp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = containerColor,
                focusedContainerColor = containerColor,
                unfocusedBorderColor = if (isError) Color.Red.copy(alpha = 0.5f) else Color.Transparent,
                focusedBorderColor = if (isError) Color.Red else Color.Transparent,
                errorContainerColor = containerColor,
                errorBorderColor = Color.Red.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions
        )
        if (helperText != null) {
            Text(
                text = helperText,
                color = Color.Red.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
private fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(InputBackground, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, text ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Primary else Color.Transparent)
                    .clickable { onOptionSelected(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = if (isSelected) Color.Black else TextSecondaryDark,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                "Pago de Ligas",
                color = TextPrimaryDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = TextPrimaryDark)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
    )
}

@Composable
private fun BottomBar(onSave: () -> Unit, enabled: Boolean) {
    BottomAppBar(
        containerColor = BackgroundDark.copy(alpha = 0.8f),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor = Color.Black
            ),
            enabled = enabled
        ) {
            Text(
                "Guardar Pago",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
