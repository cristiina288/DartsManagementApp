package org.darts.dartsmanagement.ui.collections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

// --- Color Palette ---
private val BackgroundDark = Color(0xFF0B0F13)
private val SurfaceDark = Color(0xFF111417)
private val InputBackground = Color(0xFF273A38)
private val Primary = Color(0xFF00BDA4)
private val TextPrimaryDark = Color.White
private val TextSecondaryDark = Color.White.copy(alpha = 0.8f)
private val TextPlaceholder = Color.White.copy(alpha = 0.5f)

object CollectionScreen : Screen {
    @Composable
    override fun Content() {
        CollectionScreenContent()
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
private fun CollectionScreenContent() {
    val viewModel = koinViewModel<CollectionsViewModel>()
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

    Scaffold(
        containerColor = BackgroundDark,
        topBar = { TopBar() },
        bottomBar = {
            BottomBar(onSave = { viewModel.saveActualCollection() })
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
            item { BasicDataSection(viewModel) }
            item { DistributionSection(collection) }
            item { ObservationsSection(viewModel) }
            // item { PaymentSection(viewModel) } // Temporarily commented out
        }
    }
}

@Composable
private fun BasicDataSection(viewModel: CollectionsViewModel) {
    val bars by viewModel.bars.collectAsState()
    val collection by viewModel.collection.collectAsState()

    val selectedBar = remember(collection.barId, bars) {
        bars?.find { it?.id == collection.barId }
    }

    val selectedMachine = remember(collection.machineId, selectedBar) {
        selectedBar?.machines?.find { it.id == collection.machineId }
    }

    Section(title = "Datos Básicos") {
        // Bar Dropdown
        AppDropdown(
            label = "Bar",
            options = bars?.filterNotNull() ?: emptyList(),
            selectedOption = selectedBar,
            onOptionSelected = { bar -> viewModel.onBarSelected(bar.id ?: "") },
            optionToString = { it.name }
        )

        // Machine Dropdown
        if (collection.barId != null) {
            AppDropdown(
                label = "Máquina",
                options = selectedBar?.machines ?: emptyList(),
                selectedOption = selectedMachine,
                onOptionSelected = { machine ->
                    viewModel.saveCounterAndMachineIdCollection(
                        machine.counter,
                        machine.id
                    )
                },
                optionToString = { it.name ?: "" },
                enabled = selectedBar != null
            )
        }

        // Date Field
        /*AppTextField(
            label = "Fecha de Recaudación",
            value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()), // Placeholder
            onValueChange = {},
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = TextPlaceholder) }
        )*/

        // Total Collection Field
        AppTextField(
            label = "Recaudación Total (€)",
            value = collection.collectionAmounts?.totalCollection?.toString() ?: "",
            onValueChange = {
                val value = it.toDoubleOrNull() ?: 0.0
                //viewModel.onTotalCollectionChanged(value)
                viewModel.saveCollectionAmounts(
                    buildCollectionAmounts(value, viewModel)
                )
            },
            placeholder = "0.00",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
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
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ReadOnlyField(
                label = "Contador Antiguo",
                value = (collection.counter ?: 0).toString(),
                modifier = Modifier.weight(1f)
            )
            ReadOnlyField(
                label = "Contador Actual",
                value = ((collection.counter
                    ?: 0) + (collection.collectionAmounts?.totalCollection?.toInt()
                    ?: 0)).toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun buildCollectionAmounts(
    totalCollection: Double,
    viewModel: CollectionsViewModel
): CollectionAmountsModel {
    val valueBarAmount = (totalCollection * 0.4).toDouble()
    val valueBusinessAmount = (totalCollection * 0.6).toDouble()

    return CollectionAmountsModel(
        totalCollection = totalCollection,
        barAmount = valueBarAmount,
        barPayment = viewModel.collection.value.collectionAmounts?.barPayment ?: 0.0,
        businessAmount = valueBusinessAmount,
        extraAmount = viewModel.collection.value.collectionAmounts?.extraAmount ?: 0.0
    )
}


@Composable
private fun DistributionSection(collection: CollectionsState) { // Changed to CollectionModel
    val businessAmount = collection.collectionAmounts?.businessAmount ?: 0.0
    val barAmount = collection.collectionAmounts?.barAmount ?: 0.0
    val total = businessAmount + barAmount
    val businessPercentage = if (total > 0) (businessAmount / total).toFloat() else 0.6f

    fun formatCurrency(amount: Double): String {
        val rounded = (amount * 100).toLong()
        val integerPart = rounded / 100
        val fractionalPart = rounded % 100
        return "$integerPart,${fractionalPart.toString().padStart(2, '0')} €"
    }

    Section(title = "Distribución") {
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
    textStyle: androidx.compose.ui.text.TextStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column {
        if (label != null) {
            Text(
                text = label,
                color = TextSecondaryDark,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextPlaceholder) },
            readOnly = readOnly,
            singleLine = singleLine,
            enabled = enabled,
            textStyle = textStyle.copy(color = TextPrimaryDark),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = InputBackground,
                focusedContainerColor = InputBackground,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions
        )
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

@Composable
private fun ReadOnlyField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
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
private fun BottomBar(onSave: () -> Unit) {
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
            )
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