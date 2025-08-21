package org.darts.dartsmanagement.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.darts.dartsmanagement.ui.home.bars.listing.BarsListingScreen
import org.darts.dartsmanagement.ui.home.collections.CollectionScreen
import org.darts.dartsmanagement.ui.home.locations.listing.LocationsListingScreen
import org.darts.dartsmanagement.ui.home.machines.listing.MachinesListingScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/*@Preview
@Composable
fun HomeScreen() {
    Scaffold (
        topBar = { TopBar() },
    ) {
        Column (
            modifier = Modifier.fillMaxSize()
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.LightGray)
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Text("Recaudaciones")
                }
            }

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.LightGray)
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Text("Bares")
                }

                Spacer(modifier = Modifier.padding(10.dp))

                Box(
                    modifier = Modifier
                        .background(Color.Blue)
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Text("Máquinas")
                }
            }

            Row (
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.LightGray)
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Text("Localzaciones")
                }

                Spacer(modifier = Modifier.padding(10.dp))

                Box(
                    modifier = Modifier
                        .background(Color.Blue)
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Text("Historial recaudaciones")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    TopAppBar(
        title = { Text(text = "Darts Management") },
    )
}*/

object HomeScreen : Screen {
    @Composable
    override fun Content() {
        HomeScreenContent()
    }
}


@Preview
@Composable
private fun HomeScreenContent() {
    val navigator = LocalNavigator.currentOrThrow
    val homeViewModel = koinViewModel<HomeViewModel>()

    val collections by homeViewModel.collection.collectAsState()

    val excelExporter = ExcelExporterFactory.create()

    Scaffold(
        containerColor = Color(0xFF1E2832),
        topBar = { TopBar2() },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
               // .background(Color(0xFFFDF6F3)) // Color suave de fondo
        ) {
            // Tarjeta grande: Recaudaciones
            CardItem(
                title = "Recaudaciones",
                icon = Icons.Default.Star,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(180.dp),
                colors = listOf(Color(0xFFB2FEFA), Color(0xFF0ED2F7))
            ) { navigator.push(CollectionScreen) }

            // Fila: Bares y Máquinas
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CardItem(
                    title = "Bares",
                    icon = Icons.Default.Call,
                    modifier = Modifier.weight(1f),
                    colors = listOf(Color(0xFFFFE29F), Color(0xFFFFA99F)),
                    onClick = { navigator.push(BarsListingScreen) }
                )
                CardItem(
                    title = "Máquinas",
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f),
                    colors = listOf(Color(0xFF81FBB8), Color(0xFF28C76F)),
                    onClick = { navigator.push(MachinesListingScreen) }
                )
            }

            // Fila: Localizaciones y Historial
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CardItem(
                    title = "Localizaciones",
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f),
                    colors = listOf(Color(0xFFFFD3A5), Color(0xFFFD6585)),
                    onClick = { navigator.push(LocationsListingScreen) }
                )
                CardItem(
                    title = "Historial de recaudaciones", //ahora descargará los datos
                    icon = Icons.Default.Menu,
                    modifier = Modifier.weight(1f),
                    colors = listOf(Color(0xFFA18CD1), Color(0xFFFBC2EB)),
                    onClick = {
                        homeViewModel.exportData(excelExporter)
                    }
                )
            }
        }
    }
}


@Composable
private fun CardItem(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    colors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // para que el fondo no tape el gradiente
        ),
        onClick = { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors))
                .clip(RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar2() {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        title = {
            Text(
                text = "Darts Management",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
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

