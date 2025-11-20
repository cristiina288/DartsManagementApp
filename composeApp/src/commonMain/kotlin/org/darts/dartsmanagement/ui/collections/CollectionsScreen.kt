package org.darts.dartsmanagement.ui.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel
import org.darts.dartsmanagement.domain.machines.model.MachineModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI


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
    val collectionsViewModel = koinViewModel<CollectionsViewModel>()

    val state by collectionsViewModel.state.collectAsState()
    val bars by collectionsViewModel.bars.collectAsState()
    val collection by collectionsViewModel.collection.collectAsState()
    val oldCounter = collection.counter ?: 0
    val newCounter = oldCounter + (collection.collectionAmounts?.totalCollection ?: 0.0)

    Scaffold(
        topBar = { TopBar() },
        containerColor = Color(0xFF1E2832)
    ) {
        LazyColumn(
            modifier = Modifier.padding(vertical = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.padding(vertical = 32.dp))
            }

            item {
                Text(
                    text = "Datos básicos",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style =  MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            item {
                Spacer(modifier = Modifier.padding(vertical = 12.dp))
                Text(
                    text = "Busca el bar que estás recaudando",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style =  MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            item {
                if (!bars.isNullOrEmpty()) {
                    SetDropdownBarMenu(bars, Modifier, collectionsViewModel)
                }
            }

            item {
                Column (modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Spacer(modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        text = "Fecha",
                        style =  MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    InputFecha(Modifier.fillMaxWidth())
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Spacer(modifier = Modifier.padding(vertical = 12.dp))
                        Text(
                            text = "Recaudación",
                            style =  MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        InputCollection(Modifier.fillMaxWidth(), collectionsViewModel)

                        Text(
                            text = "Contador antiguo: $oldCounter",
                            modifier = Modifier.padding(top = 4.dp),
                            style = TextStyle(
                                fontSize = TextUnit(12F, TextUnitType.Sp)
                            ),
                            color = Color.White
                        )

                        Text(
                            text = "Contador actual: $newCounter",
                            modifier = Modifier.padding(top = 4.dp),
                            style = TextStyle(
                                fontSize = TextUnit(12F, TextUnitType.Sp)
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Distribución",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style =  MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                ) {
                    Column {
                        Spacer(modifier = Modifier.padding(vertical = 4.dp))
                        var businessAmount =
                            (collection.collectionAmounts?.businessAmount ?: 0).toString()
                        var barAmount = (collection.collectionAmounts?.barAmount ?: 0).toString()

                        Text(
                            text = "Empresa",
                            style =  MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        Row {
                            Text(
                                text = "60% = ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )

                            Text(
                                text = "$businessAmount€",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )

                        }

                        Spacer(modifier = Modifier.padding(vertical = 12.dp))

                        Text(
                            text = "Bar",
                            style =  MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        Row {
                            Text(
                                text = "40% = ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )

                            Text(
                                text = "$barAmount€",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Spacer(modifier = Modifier.padding(vertical = 12.dp))
                        Text(
                            text = "Pago bar",
                            style =  MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        Text(
                            text = "(máx. el importe ganado)",
                            style = TextStyle(
                                fontSize = TextUnit(10F, TextUnitType.Sp)
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.padding(vertical = 2.dp))

                        InputCollectionPayment(Modifier, collectionsViewModel)
                    }

                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Spacer(modifier = Modifier.padding(vertical = 12.dp))
                        Text(
                            text = "Pago extra",
                            style =  MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        Text(
                            text = "(a parte de lo recaudado)",
                            style = TextStyle(
                                fontSize = TextUnit(10F, TextUnitType.Sp)
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.padding(vertical = 2.dp))

                        InputCollectionExtra(Modifier, collectionsViewModel)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Observaciones",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style =  MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            item {
                InputDescription(Modifier.padding(horizontal = 20.dp).fillMaxWidth())
            }

            item {
                Spacer(modifier = Modifier.padding(vertical = 12.dp))

                Button(
                    onClick = { collectionsViewModel.saveActualCollection() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0EA5E9)
                    )
                ) { Text("GUARDAR") }
            }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    val navigator = LocalNavigator.currentOrThrow

    TopAppBar(
        title = {
            Text(
                text = "Recaudación",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        },
        navigationIcon = {
            IconButton(onClick = { navigator.pop() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarColors(
            containerColor = Color(0xFF1E2832),
            scrolledContainerColor = TopAppBarDefaults.topAppBarColors().scrolledContainerColor,
            navigationIconContentColor = TopAppBarDefaults.topAppBarColors().navigationIconContentColor,
            titleContentColor = TopAppBarDefaults.topAppBarColors().titleContentColor,
            actionIconContentColor = TopAppBarDefaults.topAppBarColors().actionIconContentColor,
        )
    )
}


@Composable
private fun InputDescription(modifier: Modifier) {
    var valueInput by remember { mutableStateOf("") }

    OutlinedTextField(
        value = valueInput,
        onValueChange = { it ->
            valueInput = it
        },
        modifier = modifier.clip(shape = RoundedCornerShape(12.dp)),
        enabled = true,
        readOnly = true,
        textStyle = TextStyle(color = Color.White),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.DarkGray,
            unfocusedBorderColor = Color.Gray,
            disabledBorderColor = Color.LightGray,
            errorBorderColor = Color.Red
        )
    )
}


@Composable
private fun InputCollection(modifier: Modifier, viewModel: CollectionsViewModel) {
    var valueInput by remember { mutableStateOf("") }

    OutlinedTextField(
        value = valueInput,
        onValueChange = { it ->
            valueInput = it
            var value = 0.0

            value = if (it.isEmpty()) {
                0.0
            } else {
                it.toDouble()
            }

            viewModel.saveCollectionAmounts(
                buildCollectionAmounts(value, viewModel)
            )
        },
        modifier = modifier,
        enabled = true,
        readOnly = false,
            textStyle = TextStyle(color = Color.White),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.DarkGray,
            unfocusedBorderColor = Color.Gray,
            disabledBorderColor = Color.LightGray,
            errorBorderColor = Color.Red
        )
    )
}


private fun buildCollectionAmounts(totalCollection: Double, viewModel: CollectionsViewModel): CollectionAmountsModel {
    val valueBarAmount = (totalCollection*0.4).toDouble()
    val valueBusinessAmount = (totalCollection*0.6).toDouble()

    return CollectionAmountsModel(
        totalCollection = totalCollection,
        barAmount = valueBarAmount,
        barPayment = viewModel.collection.value.collectionAmounts?.barPayment ?: 0.0,
        businessAmount = valueBusinessAmount,
        extraAmount = viewModel.collection.value.collectionAmounts?.extraAmount ?: 0.0
    )
}



@Composable
private fun InputCollectionPayment(modifier: Modifier, viewModel: CollectionsViewModel) {
    var valueInput by remember { mutableStateOf((viewModel.collection.value.collectionAmounts?.barPayment ?: 0.0).toString()) }
    var isError by remember { mutableStateOf(false) }
    val maxValue = viewModel.collection.value.collectionAmounts?.barAmount ?: 0.0

    OutlinedTextField(
        value = valueInput,
        onValueChange = { it ->
            valueInput = it

            var value = if (it.isEmpty()) {
                0.0
            } else {
                it.toDouble()
            }

            if (value > maxValue) {
                isError = true
                value = 0.0
            } else {
                isError = false
            }

            viewModel.saveCollectionAmounts(
                CollectionAmountsModel(
                    totalCollection = viewModel.collection.value.collectionAmounts?.totalCollection ?: 0.0,
                    barAmount = viewModel.collection.value.collectionAmounts?.barAmount ?: 0.0,
                    barPayment = value,
                    businessAmount = viewModel.collection.value.collectionAmounts?.businessAmount ?: 0.0,
                    extraAmount = viewModel.collection.value.collectionAmounts?.extraAmount ?: 0.0
                )
            )
        },
        modifier = modifier,
        enabled = true,
        readOnly = false,
        textStyle = TextStyle(color = Color.White),
        isError = isError,
        supportingText = {
            if (isError) {
                Text("El valor no puede ser mayor que $maxValue. Cuidado que si no es correcto no se guardará este valor.")
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.DarkGray,
            unfocusedBorderColor = Color.Gray,
            disabledBorderColor = Color.LightGray,
            errorBorderColor = Color.Red
        )
    )
}


@Composable
private fun InputCollectionExtra(modifier: Modifier, viewModel: CollectionsViewModel) {
    var valueInput by remember { mutableStateOf((viewModel.collection.value.collectionAmounts?.extraAmount ?: 0).toString()) }

    OutlinedTextField(
        value = valueInput,
        onValueChange = { it ->
            valueInput = it

            var value = 0.0

            value = if (it.isEmpty()) {
                0.0
            } else {
                it.toDouble()
            }

            viewModel.saveCollectionAmounts(
                CollectionAmountsModel(
                    totalCollection = viewModel.collection.value.collectionAmounts?.totalCollection ?: 0.0,
                    barAmount = viewModel.collection.value.collectionAmounts?.barAmount ?: 0.0,
                    barPayment = viewModel.collection.value.collectionAmounts?.barPayment ?: 0.0,
                    businessAmount = viewModel.collection.value.collectionAmounts?.businessAmount ?: 0.0,
                    extraAmount = value
                )
            )
        },
        modifier = modifier,
        enabled = true,
        readOnly = false,
        textStyle = TextStyle(color = Color.White),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.DarkGray,
            unfocusedBorderColor = Color.Gray,
            disabledBorderColor = Color.LightGray,
            errorBorderColor = Color.Red
        )
    )
}


@Composable
private fun InputFecha(modifier: Modifier) {
    var valueInput by remember { mutableStateOf("") }

    OutlinedTextField(
        value = valueInput,
        onValueChange = { it ->
            valueInput = it
        },
        modifier = modifier,
        enabled = true,
        readOnly = false,
        textStyle = TextStyle(color = Color.White),
        placeholder = { Text("dd/mm/yyyy") },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.DarkGray,
            unfocusedBorderColor = Color.Gray,
            disabledBorderColor = Color.LightGray,
            errorBorderColor = Color.Red
        )
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetDropdownBarMenu(bars: List<BarModel?>?, modifier: Modifier, viewModel: CollectionsViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }
    var barSelected: BarModel? by remember { mutableStateOf(null) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { selectedText = it },
            readOnly = true,
            label = { Text("Selecciona una opción") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), // Esto sí redondea el borde visible
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color.Gray,
                disabledBorderColor = Color.LightGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color(0xFF1E2832)
        ) {
            bars?.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option?.name ?: "") },
                    onClick = {
                        selectedText = option?.name ?: ""
                        expanded = false
                        barSelected = option
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = Color.White
                    )
                )
            }
        }
    }

    if (!bars.isNullOrEmpty() && barSelected != null) {
        Spacer(modifier = modifier.padding(vertical = 12.dp))
        Text(
            text = "Selecciona la máquina que estás recaudando",
            modifier = modifier.padding(horizontal = 20.dp),
            style =  MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        SetDropdownMachines(barSelected!!.machines, modifier, viewModel)
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetDropdownMachines(machines: List<MachineModel>, modifier: Modifier, viewModel: CollectionsViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { selectedText = it },
            readOnly = true,
            label = { Text("Selecciona una opción") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), // Esto sí redondea el borde visible
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color.Gray,
                disabledBorderColor = Color.LightGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color(0xFF1E2832),
        ) {
            machines.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name ?: "") },
                    onClick = {
                        selectedText = option.name ?: ""
                        expanded = false
                        viewModel.saveCounterAndMachineIdCollection(option.counter, option.id)
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = Color.White
                    )
                )
            }
        }
    }
}




/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerScreen(
    modifier: Modifier = Modifier
) {
    val datePickerState = rememberDatePickerState()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DatePicker(state = datePickerState)
        Text(text = "Selected ${datePickerState.selectedDateMillis}")
    }
}*/


/*
private fun obtenerFechaActual(): String {
    val fechaActual = LocalDate.now()  // Obtiene la fecha del sistema
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")  // Define el formato
    return fechaActual.format(formatter)  // Devuelve la fecha formateada
}
*/


/*val datePickerState = rememberDatePickerState()
var openDialog by remember { mutableStateOf(false) }
var fechaSeleccionada by remember { mutableStateOf(obtenerFechaActual()) }

if (openDialog) {
    DatePickerDialog(
        onDismissRequest = { openDialog = false },
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val localDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    fechaSeleccionada = localDate.format(formatter)
                }
                openDialog = false
            }) {
                Text("Aceptar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

TextField(
    value = fechaSeleccionada,
    onValueChange = {},
    readOnly = true,
    label = { Text("Selecciona una fecha") },
    trailingIcon = {
        IconButton(onClick = { openDialog = true }) {
            Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
        }
    },
    modifier = Modifier.fillMaxWidth()
)*/