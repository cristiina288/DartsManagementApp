package org.darts.dartsmanagement.ui.collections.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

data class EditCollectionScreen(val collection: CollectionModel) : Screen {
    @Composable
    override fun Content() {
        EditCollectionScreenContent(collection)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCollectionScreenContent(collection: CollectionModel) {
    val navigator = LocalNavigator.currentOrThrow
    val viewModel = koinViewModel<EditCollectionViewModel>(parameters = { parametersOf(collection) })
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navigator.pop()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Editar Recaudación",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background.copy(alpha = 0.8f)
                ),
                actions = {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Background.copy(alpha = 0.8f)
            ) {
                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Button(
                    onClick = { viewModel.save() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = if (uiState.isLoading) "Guardando..." else "Guardar Cambios",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bar: ${collection.barName}",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            val machineIds = collection.machinesCollection.map { it.machineId }
            Text(
                text = if (machineIds.size == 1) "Máquina ID: ${machineIds.first()}" else "Máquinas: ${machineIds.joinToString(", ")}",
                color = TextPrimary,
                fontSize = 16.sp
            )

            HorizontalDivider(color = Border)

            OutlinedTextField(
                value = uiState.totalCollection,
                onValueChange = { viewModel.onTotalCollectionChange(it) },
                label = { Text("Recaudación Total Bruta (€)", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = textFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.totalBarAmount,
                    onValueChange = { viewModel.onTotalBarAmountChange(it) },
                    label = { Text("Total Bar (€)", color = TextSecondary) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = uiState.totalBusinessAmount,
                    onValueChange = { viewModel.onTotalBusinessAmountChange(it) },
                    label = { Text("Total Empresa (€)", color = TextSecondary) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            OutlinedTextField(
                value = uiState.comments,
                onValueChange = { viewModel.onCommentsChange(it) },
                label = { Text("Comentarios", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = textFieldColors(),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
private fun textFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryAccent.copy(alpha = 0.5f),
    unfocusedBorderColor = Border,
    focusedLabelColor = TextSecondary,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = InputBackground,
    unfocusedContainerColor = InputBackground,
    cursorColor = PrimaryAccent,
)
