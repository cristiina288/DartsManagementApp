package org.darts.dartsmanagement.ui.collections

import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SheetState
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dartsmanagement.composeapp.generated.resources.Res
import dartsmanagement.composeapp.generated.resources.ico_upload
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.ui.collections.edit.EditCollectionScreen
import org.darts.dartsmanagement.ui.theme.Background
import org.darts.dartsmanagement.ui.theme.Dimens
import org.darts.dartsmanagement.ui.theme.ElevatedSurface
import org.darts.dartsmanagement.ui.theme.PrimaryAccent
import org.darts.dartsmanagement.ui.theme.TextPrimary
import org.darts.dartsmanagement.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

object HistorialCollectionsScreen : Screen {
    @Composable
    override fun Content() {
        HistorialCollectionsScreenContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialCollectionsScreenContent() {
    val navigator = LocalNavigator.currentOrThrow
    val viewModel = koinViewModel<HistorialCollectionsViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        viewModel.onEvent(HistorialCollectionsEvent.Refresh)
        onPauseOrDispose { }
    }

    LaunchedEffect(listState.canScrollForward, uiState.canLoadMore, uiState.isLoading) {
        // If we're at the end of the list, can load more, and not currently loading
        if (!listState.canScrollForward && uiState.canLoadMore && !uiState.isLoading) {
            viewModel.onEvent(HistorialCollectionsEvent.LoadMoreCollections)
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(
                            painter = painterResource(Res.drawable.ico_upload),
                            contentDescription = "Export",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.collections.isEmpty() && !uiState.isLoading) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(uiState.collections) { index, collection ->
                        val localDateTime = collection.createdAt.toLocalDateTime()
                        val currentMonthYear = localDateTime.date.formatMonthYear()
                        val previousMonthYear = if (index > 0) {
                            uiState.collections[index - 1].createdAt.toLocalDateTime().date.formatMonthYear()
                        } else null

                        if (index == 0 || currentMonthYear != previousMonthYear) {
                            MonthSeparator(monthYear = currentMonthYear)
                        }
                        
                        var showDeleteConfirmation by remember { mutableStateOf<String?>(null) }
                        
                        CollectionAccordionItem(
                            collection = collection, 
                            localDateTime = localDateTime,
                            onEditClick = { navigator.push(EditCollectionScreen(collection)) },
                            onDeleteClick = { showDeleteConfirmation = collection.id }
                        )

                        if (showDeleteConfirmation == collection.id) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirmation = null },
                                title = { Text("Eliminar Recaudación") },
                                text = { Text("¿Estás seguro de que deseas eliminar esta recaudación? Esta acción no se puede deshacer.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.onEvent(HistorialCollectionsEvent.DeleteCollection(collection.id))
                                            showDeleteConfirmation = null
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Eliminar")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirmation = null }) {
                                        Text("Cancelar")
                                    }
                                },
                                containerColor = ElevatedSurface,
                                titleContentColor = TextPrimary,
                                textContentColor = TextSecondary
                            )
                        }
                    }

                    if (uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PrimaryAccent)
                            }
                        }
                    }
                }
            }

            if (uiState.error != null) {
                // TODO: Implement a better error display
                Text(
                    text = uiState.error ?: "Unknown Error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(8.dp)
                )
            }
        }

        if (showBottomSheet) {
            ExportCollectionsBottomSheet(
                sheetState = sheetState,
                onDismiss = { showBottomSheet = false },
                onExport = { date ->
                    viewModel.onEvent(HistorialCollectionsEvent.ExportCollections(date))
                    println("Exporting collections from: $date")
                    showBottomSheet = false
                }
            )
        }
    }
}

@Composable
fun MonthSeparator(monthYear: String) {
    Text(
        text = monthYear,
        color = TextSecondary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
fun CollectionAccordionItem(
    collection: CollectionModel, 
    localDateTime: LocalDateTime,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
    val isExpanded = expandedStates[collection.id.toString()] ?: false

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cornerRadiusBig))
            .background(ElevatedSurface)
            .clickable { expandedStates[collection.id.toString()] = !isExpanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = localDateTime.date.month.name.substring(0, 3),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = localDateTime.date.dayOfMonth.toString(),
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(text = "${collection.barName}, Máquina #${collection.machineId}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "${collection.totalCollection.formatCurrency()} €", color = TextSecondary, fontSize = 14.sp)
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Expand",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Recaudación Bruta:", color = TextPrimary, fontSize = 14.sp)
                    Text("${collection.totalCollection.formatCurrency()} €", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Parte Bar:", color = TextPrimary, fontSize = 14.sp)
                    Text("${collection.barAmount.formatCurrency()} €", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Parte Empresa:", color = TextPrimary, fontSize = 14.sp)
                    Text("${collection.businessAmount.formatCurrency()} €", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        collection.comments?.let {
                            if (it.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(it, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                    
                    Row {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = PrimaryAccent.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Double.formatCurrency(): String {
    val integerPart = this.toLong()
    val fractionalPart = ((this - integerPart) * 100).toLong()
    return "$integerPart,${fractionalPart.toString().padStart(2, '0')}"
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TODO: Add a proper empty state icon if available
        Text(
            text = "No hay registros de recaudación.",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Cuando realices una, aparecerá aquí.",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

private fun Long.toLocalDateTime(): LocalDateTime {
    return Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
}

private fun LocalDate.formatMonthYear(): String {
    val month = this.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$month ${this.year}"
}
