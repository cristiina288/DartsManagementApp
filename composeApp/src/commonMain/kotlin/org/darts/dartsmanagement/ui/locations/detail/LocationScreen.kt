package org.darts.dartsmanagement.ui.locations.detail

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dartsmanagement.composeapp.generated.resources.Res
import dartsmanagement.composeapp.generated.resources.ico_beer
import org.darts.dartsmanagement.ui.bars.detail.BarScreen
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.locations.model.LocationModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val BackgroundDark = Color(0xFF121212)
private val SurfaceDark = Color(0xFF1E1E1E)
private val Primary = Color(0xFF00BDA4)
private val TextPrimaryDark = Color(0xFFE0E0E0)
private val TextSecondaryDark = Color(0xFFB0B0B0)
private val BorderDark = Color.White.copy(alpha = 0.1f)


class LocationScreen (val location: LocationModel) : Screen {
    @Composable
    override fun Content() {
        LocationScreenContent(location)
    }
}

@Composable
fun LocationScreenContent(
    location: LocationModel,
    onEditClick: () -> Unit = {},
) {
    val navigator = LocalNavigator.currentOrThrow
    val uriHandler = LocalUriHandler.current

    val locationViewModel = koinViewModel<LocationViewModel>(
        parameters = { parametersOf(location.id) }
    )
    val bars by locationViewModel.bars.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(
                title = "Detalles de la Ubicación",
                onBackClick = { navigator.pop() },
                onEditClick = { /* TODO: Implement edit logic */ }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = location.name.orEmpty(),
                    color = TextPrimaryDark,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp
                )
                Text(
                    text = "Código Postal: ${location.postalCode}",
                    color = TextSecondaryDark,
                    fontSize = 16.sp
                )
            }

            item {
                Text(
                    text = "Bares Asignados",
                    color = TextPrimaryDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                if (bars.isNotEmpty()) { // Use the bars from the ViewModel
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        bars.forEach { bar ->
                            BarListItem(bar = bar, onClick = { navigator.push(BarScreen(bar.id ?:  "")) })
                        }
                    }
                } else {
                    Text(
                        text = "Sin bares asignados",
                        color = TextSecondaryDark,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BarListItem(
    bar: BarModel,
    onClick: () -> Unit
) {
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
                .padding(vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ico_beer),
                contentDescription = bar.name,
                tint = Primary,
                modifier = Modifier
                    .size(42.dp)
                    .padding(start = 16.dp)
            )

            Spacer(Modifier.width(16.dp))

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
                    text = bar.location.address ?: "Ubicación no disponible",
                    color = TextSecondaryDark,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .background(Primary.copy(alpha = 0.2f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
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
private fun TopBar(title: String, onBackClick: () -> Unit, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark) // Use same BackgroundDark as screen
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = TextPrimaryDark
            )
        }
        Text(
            text = title,
            color = TextPrimaryDark,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onEditClick) {
            Text(
                text = "Editar",
                color = Primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
