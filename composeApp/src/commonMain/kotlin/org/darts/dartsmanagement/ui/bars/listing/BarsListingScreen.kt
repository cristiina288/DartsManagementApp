package org.darts.dartsmanagement.ui.bars.listing

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.ui.bars.detail.BarScreen
import org.darts.dartsmanagement.ui.bars.newBar.NewBarScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.jetbrains.compose.resources.painterResource
import dartsmanagement.composeapp.generated.resources.*

// New Color Palette based on HTML
val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)
val Primary = Color(0xFF00BDA4)
val TextPrimaryDark = Color(0xFFE0E0E0)
val TextSecondaryDark = Color(0xFFB0B0B0)
val BorderDark = Color.White.copy(alpha = 0.1f)

object BarsListingScreen : Screen {
    @Composable
    override fun Content() {
        BarsListingScreenContent()
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
private fun BarsListingScreenContent() {
    val barsListingViewModel = koinViewModel<BarsListingViewModel>()
    val bars by barsListingViewModel.bars.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(navigator.lastItem) {
        barsListingViewModel.loadBars()
    }

    Scaffold(
        //modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = BackgroundDark,
        topBar = { TopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigator.push(NewBarScreen) },
                containerColor = Primary,
                contentColor = BackgroundDark,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar bar",
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
            // Search Bar
            SearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val filteredBars = bars?.filter {
                    it?.name?.contains(searchQuery, ignoreCase = true) == true ||
                            it?.location?.address?.contains(searchQuery, ignoreCase = true) == true
                } ?: emptyList()

                items(filteredBars, key = { it?.id ?: 0 }) { bar ->
                    bar?.let {
                        BarListItem(bar = it, onClick = {
                            navigator.push(BarScreen(bar.id!!))
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Buscar bar...", color = TextSecondaryDark) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondaryDark
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SurfaceDark,
            unfocusedContainerColor = SurfaceDark,
            disabledContainerColor = SurfaceDark,
            focusedTextColor = TextPrimaryDark,
            unfocusedTextColor = TextPrimaryDark,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}


@Composable
fun BarListItem(bar: BarModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp) // Maintain 8.dp padding as requested
        ) {
            // Replaced accent bar with ico-beer.xml
            Icon(
                painter = painterResource(Res.drawable.ico_beer),
                contentDescription = bar.name,
                tint = Primary,
                modifier = Modifier
                    .size(42.dp)
                    .padding(start = 16.dp)
            )

            Spacer(Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = bar.name,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = bar.location?.address ?: "Ubicación no disponible",
                    color = TextSecondaryDark,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(16.dp))

            // Machine count chip
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .background(Primary.copy(alpha = 0.2f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // TODO: PENDING NUMBER OF BARS OR NUMBER OF MACHINES ???
                Text(
                    text = (bar.machines.count()).toString(),
                    color = Primary,
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

    TopAppBar(
        title = {
            Text(
                text = "Bares",
                color = TextPrimaryDark,
                fontSize = 22.sp, // Matching LocationsListingScreen.kt
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = { navigator.pop() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimaryDark
                )
            }
        },
       /* colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0B0F13).copy(alpha = 0.8f)
        ),*/
        actions = {
            Spacer(modifier = Modifier.width(48.dp)) // To balance the title
        }
    )
}


