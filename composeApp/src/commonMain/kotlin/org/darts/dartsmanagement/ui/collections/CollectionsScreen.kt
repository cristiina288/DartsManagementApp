package org.darts.dartsmanagement.ui.collections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel
import org.darts.dartsmanagement.ui.theme.SecondaryAccent
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

// --- Color Palette ---
private val BackgroundDark = Color(0xFF0B0F13)
val BorderDark = Color.White.copy(alpha = 0.1f)
private val SurfaceDark = Color.White.copy(alpha = 0.1f)//Color(0xFF111417)
private val InputBackground = Color(0xFF273A38)
private val Primary = Color(0xFF00BDA4)
private val TextPrimaryDark = Color.White
private val TextSecondaryDark = Color.White.copy(alpha = 0.8f)
private val TextPlaceholder = Color.White.copy(alpha = 0.5f)

data class CollectionScreen(val barId: String? = null) : Screen {
    @Composable
    override fun Content() {
        CollectionScreenContent(barId)
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
private fun CollectionScreenContent(barId: String? = null) {
    val viewModel = koinViewModel<CollectionsViewModel>(parameters = { parametersOf(barId) })
    val collection by viewModel.collection.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navigator = LocalNavigator.currentOrThrow

    LaunchedEffect(collection.snackbarMessage) {
        collection.snackbarMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.onSnackbarShown()
                navigator.pop()
            }
        }
    }

    val isSaveButtonEnabled = collection.barId != null &&
            collection.machineEntries.isNotEmpty() &&
            collection.machineEntries.all { it.machineId != null && (it.collectionAmounts?.totalCollection ?: 0.0) > 0.0 } &&
            collection.leaguePayments.all { it.error == null }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = { TopBar() },
        bottomBar = {
            BottomBar(
                onSave = { viewModel.saveActualCollection() },
                enabled = isSaveButtonEnabled
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
                val bars by viewModel.bars.collectAsState()
                val selectedBar = remember(collection.barId, bars) {
                    bars?.find { it?.id == collection.barId }
                }

                Section(title = "Datos del Bar") {
                    SearchableAppDropdown(
                        label = "Bar",
                        options = bars?.filterNotNull() ?: emptyList(),
                        selectedOption = selectedBar,
                        onOptionSelected = { bar -> viewModel.onBarSelected(bar?.id ?: "") },
                        optionToString = { it.name }
                    )
                }
            }

            if (collection.barId != null) {
                collection.machineEntries.forEachIndexed { index, entry ->
                    item {
                        if (index > 0) {
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Spacer (modifier = Modifier.height(24.dp))
                        }
                        MachineDataSection(viewModel, index)
                    }
                }

                item {
                    AddMachineButton(viewModel)
                }

                item { GlobalExtraPaymentSection(viewModel) }

                item { TotalDistributionSection(collection) }
                item { ObservationsSection(viewModel) }

                if (collection.availableLeagues.isNotEmpty()) {
                    item { LeaguePaymentSection(viewModel) }
                    item { GeneralSummarySection(collection) }
                }
            }
        }

        if (collection.showLeagueValidationDialog) {
            LeagueValidationDialog(viewModel)
        }
    }
}

@Composable
private fun LeaguePaymentSection(viewModel: CollectionsViewModel) {
    val collection by viewModel.collection.collectAsState()
    
    Section(title = "Pagos pendientes de ligas") {
        collection.leaguePayments.forEachIndexed { index, entry ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (index > 0) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val selectedLeague = collection.availableLeagues.find { it.id == entry.leagueId }
                    
                    Box(modifier = Modifier.weight(1f)) {
                        AppDropdown(
                            label = "Liga",
                            options = collection.availableLeagues,
                            selectedOption = selectedLeague,
                            onOptionSelected = { league ->
                                viewModel.onLeagueSelected(index, league.id)
                            },
                            optionToString = { it.name }
                        )
                    }

                    if (collection.leaguePayments.size > 1) {
                        IconButton(
                            onClick = { viewModel.onRemoveLeaguePaymentEntry(index) },
                            modifier = Modifier.padding(top = 24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red.copy(alpha = 0.6f))
                        }
                    }
                }

                AppTextField(
                    label = "Precio (€)",
                    value = entry.amount,
                    onValueChange = { viewModel.onLeagueAmountChanged(index, it) },
                    placeholder = "0.00",
                    isError = entry.error != null,
                    helperText = entry.error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Text("€", color = TextPlaceholder) }
                )
            }
        }

        if (collection.leaguePayments.size < collection.availableLeagues.size) {
            TextButton(
                onClick = { viewModel.onAddLeaguePaymentEntry() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Añadir otro pago de liga", fontSize = 14.sp, color = Primary)
            }
        }
    }
}

@Composable
private fun GeneralSummarySection(collection: CollectionsState) {
    val totalInitialBusinessAmount = collection.machineEntries.sumOf { it.collectionAmounts?.businessAmount ?: 0.0 }
    val totalInitialBarAmount = collection.machineEntries.sumOf { it.collectionAmounts?.barAmount ?: 0.0 }
    val globalExtraAmount = collection.globalExtraPayment
    val leagueTotalAmount = collection.leaguePayments.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }

    val totalBusinessAmount = totalInitialBusinessAmount + globalExtraAmount + leagueTotalAmount
    val totalBarAmount = totalInitialBarAmount - globalExtraAmount - leagueTotalAmount

    fun formatCurrency(amount: Double): String {
        val rounded = (amount * 100).toLong()
        val integerPart = rounded / 100
        val fractionalPart = rounded % 100
        return "$integerPart,${fractionalPart.toString().padStart(2, '0')} €"
    }

    Section(title = "Resumen general (Inc. Ligas)") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryBox(
                label = "Total Empresa",
                amount = formatCurrency(totalBusinessAmount),
                color = Primary,
                modifier = Modifier.weight(1f)
            )
            SummaryBox(
                label = "Total Bar",
                amount = formatCurrency(totalBarAmount),
                color = SecondaryAccent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryBox(
    label: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(text = label, color = TextSecondaryDark, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = amount, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LeagueValidationDialog(viewModel: CollectionsViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.onDismissLeagueValidation() },
        title = { Text("Pago de liga pendiente") },
        text = { Text("Tienes pendiente rellenar el pago de la liga, ¿quieres guardar el resto de la recaudación y salir o continuar y rellenar el dato que falta?") },
        confirmButton = {
            TextButton(onClick = { viewModel.saveActualCollection(ignoreLeagueValidation = true) }) {
                Text("Guardar y salir")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onDismissLeagueValidation() }) {
                Text("Rellenar el dato")
            }
        },
        containerColor = SurfaceDark,
        titleContentColor = TextPrimaryDark,
        textContentColor = TextSecondaryDark
    )
}

@Composable
private fun MachineDataSection(viewModel: CollectionsViewModel, index: Int) {
    val bars by viewModel.bars.collectAsState()
    val collection by viewModel.collection.collectAsState()
    val entry = collection.machineEntries[index]

    val selectedBar = remember(collection.barId, bars) {
        bars?.find { it?.id == collection.barId }
    }

    val selectedMachine = remember(entry.machineId, selectedBar) {
        selectedBar?.machines?.find { it.id == entry.machineId }
    }

    val selectedMachineIds = collection.machineEntries.mapNotNull { it.machineId }
    val availableMachines = selectedBar?.machines?.filter { 
        it.id == entry.machineId || !selectedMachineIds.contains(it.id) 
    } ?: emptyList()

    Section(title = if (collection.machineEntries.size > 1) "Máquina ${index + 1}" else "Datos Básicos") {
        // Machine Dropdown
        AppDropdown(
            label = "Máquina",
            options = availableMachines,
            selectedOption = selectedMachine,
            onOptionSelected = { machine ->
                viewModel.saveCounterAndMachineIdCollection(
                    machine.counter,
                    machine.id,
                    index
                )
            },
            optionToString = { it.name ?: "" },
            enabled = selectedBar != null
        )

        Spacer(Modifier.height(8.dp))

        SegmentedToggle(
            options = listOf("Recaudación", "Contador"),
            selectedIndex = if (entry.inputMode == CollectionInputMode.COLLECTION) 0 else 1,
            onOptionSelected = { idx ->
                val mode = if (idx == 0) CollectionInputMode.COLLECTION else CollectionInputMode.COUNTER
                viewModel.onInputModeChanged(mode, index)
            }
        )

        // Total Collection Field
        AppTextField(
            label = "Recaudación Total (€)",
            value = entry.recaudacionInput,
            onValueChange = {
                viewModel.saveCollectionAmounts(it, index)
            },
            placeholder = "0.00",
            readOnly = entry.inputMode == CollectionInputMode.COUNTER,
            containerColor = if (entry.inputMode == CollectionInputMode.COLLECTION) InputBackground else InputBackground.copy(alpha = 0.2f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 28.sp, 
                fontWeight = FontWeight.Bold,
                color = if (entry.inputMode == CollectionInputMode.COLLECTION) Primary else TextPrimaryDark
            ),
            trailingIcon = {
                Text(
                    "€",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPlaceholder
                )
            }
        )

        // Counters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ReadOnlyField(
                label = "Contador Antiguo",
                value = (entry.counter ?: 0).toString(),
                modifier = Modifier.weight(1f)
            )
            
            val isCounterMode = entry.inputMode == CollectionInputMode.COUNTER

            AppTextField(
                label = "Contador Actual",
                value = entry.contadorInput,
                onValueChange = {
                    viewModel.onCurrentCounterChanged(it, index)
                },
                readOnly = !isCounterMode,
                containerColor = if (isCounterMode) InputBackground else InputBackground.copy(alpha = 0.2f),
                placeholder = (entry.counter ?: 0).toString(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCounterMode) Primary else TextPrimaryDark
                )
            )
        }

        DistributionBoxSection(entry)

        if (collection.machineEntries.size > 1) {
            TextButton(
                onClick = { viewModel.onRemoveMachineEntry(index) },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.7f)),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Quitar máquina")
            }
        }
    }
}

@Composable
private fun AddMachineButton(viewModel: CollectionsViewModel) {
    val bars by viewModel.bars.collectAsState()
    val collection by viewModel.collection.collectAsState()

    val selectedBar = remember(collection.barId, bars) {
        bars?.find { it?.id == collection.barId }
    }

    val totalMachinesInBar = selectedBar?.machines?.size ?: 0
    val currentEntriesCount = collection.machineEntries.size

    // Solo permitimos añadir si hay menos entradas que máquinas en el bar
    val canAddMoreEntries = currentEntriesCount < totalMachinesInBar
    // Y solo si todas las entradas actuales tienen una máquina asignada
    val allEntriesFilled = collection.machineEntries.all { it.machineId != null }

    if (canAddMoreEntries && collection.barId != null) {
        OutlinedButton(
            onClick = { viewModel.onAddMachineEntry() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
            border = BorderStroke(1.dp, Primary.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            enabled = allEntriesFilled
        ) {
            Text("Añadir máquina")
        }
    }
}

@Composable
private fun GlobalExtraPaymentSection(viewModel: CollectionsViewModel) {
    val collection by viewModel.collection.collectAsState()
    
    Section(title = "Ajustes del Bar") {
        AppTextField(
            label = "Pago extra global (€)",
            value = if (collection.globalExtraPayment == 0.0) "" else collection.globalExtraPayment.toString(),
            onValueChange = { viewModel.onGlobalExtraPaymentChanged(it.toDoubleOrNull() ?: 0.0) },
            placeholder = "0.00",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            trailingIcon = {
                Text(
                    "€",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPlaceholder
                )
            }
        )
    }
}

@Composable
private fun DistributionBoxSection(entry: MachineCollectionEntry) {
    val businessAmount = entry.collectionAmounts?.businessAmount ?: 0.0
    val barAmount = entry.collectionAmounts?.barAmount ?: 0.0
    val total = businessAmount + barAmount
    val businessPercentage = if (total > 0) (businessAmount / total).toFloat() else 0.6f

    fun formatCurrency(amount: Double): String {
        val rounded = (amount * 100).toLong()
        val integerPart = rounded / 100
        val fractionalPart = rounded % 100
        return "$integerPart,${fractionalPart.toString().padStart(2, '0')} €"
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Distribución inicial", color = TextSecondaryDark, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DistributionBox(
                label = "Empresa (60%)",
                amount = formatCurrency(businessAmount),
                isPrimary = true,
                modifier = Modifier.weight(1f)
            )
            DistributionBox(
                label = "Bar (40%)",
                amount = formatCurrency(barAmount),
                isPrimary = false,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { businessPercentage },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = Primary.copy(alpha = 0.7f),
            trackColor = Primary.copy(alpha = 0.2f)
        )
    }
}

private fun buildCollectionAmountsForEntry(
    totalCollection: Double,
    index: Int,
    viewModel: CollectionsViewModel
): CollectionAmountsModel {
    val valueBarAmount = (totalCollection * 0.4).toDouble()
    val valueBusinessAmount = (totalCollection * 0.6).toDouble()

    return CollectionAmountsModel(
        totalCollection = totalCollection,
        barAmount = valueBarAmount,
        businessAmount = valueBusinessAmount
    )
}


@Composable
private fun DistributionSection(collection: CollectionsState) { 
    // This is no longer used directly as we have DistributionBoxSection per machine
}

@Composable
fun ExtraPaymentSection(viewModel: CollectionsViewModel, index: Int) {
    // This is no longer used per machine
}

@Composable
fun TotalDistributionSection(collection: CollectionsState) {
    val totalInitialBusinessAmount = collection.machineEntries.sumOf { it.collectionAmounts?.businessAmount ?: 0.0 }
    val totalInitialBarAmount = collection.machineEntries.sumOf { it.collectionAmounts?.barAmount ?: 0.0 }
    val globalExtraAmount = collection.globalExtraPayment

    val totalBusinessAmount = totalInitialBusinessAmount + globalExtraAmount
    val totalBarAmount = totalInitialBarAmount - globalExtraAmount

    val total = totalBusinessAmount + totalBarAmount
    val businessPercentage = if (total > 0) (totalBusinessAmount / total).toFloat() else 0.6f

    fun formatCurrency(amount: Double): String {
        val rounded = (amount * 100).toLong()
        val integerPart = rounded / 100
        val fractionalPart = rounded % 100
        return "$integerPart,${fractionalPart.toString().padStart(2, '0')} €"
    }

    Section(title = "Distribución Final (Total Bar)") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DistributionBox(
                label = "Total Empresa",
                amount = formatCurrency(totalBusinessAmount),
                isPrimary = true,
                modifier = Modifier.weight(1f)
            )
            DistributionBox(
                label = "Total Bar",
                amount = formatCurrency(totalBarAmount),
                isPrimary = false,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { businessPercentage },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = Primary.copy(alpha = 0.7f),
            trackColor = Primary.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun ObservationsSection(viewModel: CollectionsViewModel) {
    val collection by viewModel.collection.collectAsState()
    Section(title = "Comentarios") {
        AppTextField(
            value = collection.comments ?: "",
            onValueChange = { viewModel.onCommentsChange(it) },
            placeholder = "Añadir notas sobre incidencias, estado de la máquina, etc.",
            singleLine = false,
            modifier = Modifier.defaultMinSize(minHeight = 120.dp)
        )
    }
}

/*
@Composable
private fun PaymentSection(viewModel: CollectionsViewModel) {
    val collection by viewModel.collection.collectAsState()
    val barAmount = collection.collectionAmounts?.barAmount ?: 0.0

    Section(title = "Pagos") {
        AppTextField(
            label = "Pago bar (máx. %.2f)".format(barAmount),
            value = collection.collectionAmounts?.barPayment?.toString() ?: "",
            onValueChange = {
                val value = it.toDoubleOrNull() ?: 0.0
                viewModel.onBarPaymentChanged(value.coerceAtMost(barAmount))
             },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        AppTextField(
            label = "Pago extra",
            value = collection.collectionAmounts?.extraAmount?.toString() ?: "",
            onValueChange = { viewModel.onExtraPaymentChanged(it.toDoubleOrNull() ?: 0.0) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}
*/


// --- Reusable Components ---

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
    textStyle: androidx.compose.ui.text.TextStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
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
            textStyle = textStyle.copy(color = TextPrimaryDark),
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
private fun <T> SearchableAppDropdown(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    optionToString: (T) -> String,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(selectedOption?.let { optionToString(it) } ?: "") }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(selectedOption) {
        if (!isSearching) {
            searchText = selectedOption?.let { optionToString(it) } ?: ""
        }
    }

    val filteredOptions = if (!isSearching) {
        options
    } else {
        options.filter { optionToString(it).contains(searchText, ignoreCase = true) }
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
            onExpandedChange = {
                if (enabled) {
                    expanded = !expanded
                    if (!expanded) {
                        isSearching = false
                    }
                }
            },
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    isSearching = true
                    if (it.isNotEmpty()) {
                        expanded = true
                    } else {
                        expanded = false
                        onOptionSelected(null)
                    }
                },
                readOnly = false,
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
                onDismissRequest = {
                    expanded = false
                    isSearching = false
                },
                modifier = Modifier.background(SurfaceDark)
            ) {
                filteredOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionToString(option), color = TextPrimaryDark) },
                        onClick = {
                            onOptionSelected(option)
                            searchText = optionToString(option)
                            expanded = false
                            isSearching = false
                        }
                    )
                }
            }
        }
    }
}



@Composable
private fun ReadOnlyField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = TextSecondaryDark,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(InputBackground.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value,
                color = TextPrimaryDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DistributionBox(
    label: String,
    amount: String,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = if (isPrimary) Primary.copy(alpha = 0.7f) else Primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Text(text = label, color = TextSecondaryDark, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = amount, color = TextPrimaryDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    val navigator = LocalNavigator.currentOrThrow
    TopAppBar(
        title = {
            Text(
                "Recaudaciones",
                color = TextPrimaryDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = { navigator.pop() }) {
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
                "Guardar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
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