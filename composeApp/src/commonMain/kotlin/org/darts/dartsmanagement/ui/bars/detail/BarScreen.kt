package org.darts.dartsmanagement.ui.bars.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.darts.dartsmanagement.domain.bars.models.BarModel

//import coil.compose.AsyncImage

class BarScreen (val bar: BarModel) : Screen {
    @Composable
    override fun Content() {
        BarScreenContent(bar)
    }
}


@Composable
private fun BarScreenContent(bar: BarModel) {
    val backgroundColor = Color(0xFF141B1F)
    val textPrimary = Color.White
    val textSecondary = Color(0xFF9DB1BE)
    val navigator = LocalNavigator.currentOrThrow
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 5.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = textPrimary,
                modifier = Modifier.size(24.dp)
                    .clickable {
                         navigator.pop()
                    }
            )
            Text(
                text = "Bar",
                color = textPrimary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Editar",
                color = textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Banner Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(218.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
        ) {
           /* AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDDD855I2jw9dS906inAM6AL2Y1gF8vTTJgEd8paVM4nkiCuT9Y6StSnCjZhl8EtX6nWIrCs8Kk4foLcXPIto3wjWdXtJrg5ofgWGO0j5zvI6uPVvUghp8Msf_Y5kGU3E7u-EmXdp4E4E4QQ_KmlGWMuTVsKJdXD3HpoJPtVMP0prYjTSV5MV9HMlqDKMJD3Hi-jyD0bg00mLjeZmgurNlwVeSLwDxhW6MPrmgGBHHQ13Q-RnqlF0MC5C4DLcvt2KU1AwAtpWVcTxI",
                contentDescription = "Bar Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )*/
        }

        // Bar Title
        Text(
            text = bar.name,
            color = textPrimary,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Address
        Text(
            text = bar.location.name ?: "",
            color = textPrimary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        // View on Map
        if (bar.location.locationBarUrl?.isNotEmpty() == true) {
            Text(
                text = "Ver en Google Maps",
                color = textSecondary,
                style = MaterialTheme.typography.bodySmall,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clickable {
                        uriHandler.openUri(bar.location.locationBarUrl)
                    }
            )
        }


        Spacer(modifier = Modifier.padding(6.dp))

        // Assigned Machines Header
        Text(
            text = "Máquinas asignadas",
            color = textPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        if (bar.machines.isNotEmpty()) {
            // Machines
            bar.machines.forEach { machine ->
                MachineItem(
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDBwD1_0AqcGIyhlroZqFuK-jpNCCTCAdM_wzxG_0LUZX-H6gkAz5VwLp8ZGScgrLMbG6B1fYCleadXwXoUkTiZYSOFF6SRwT_Q9_m8ycT6l73MItmekcLf-X-pcoM-g8FokH5yTIgt1Y4nV5p7hCIMfWOt9xK7PeppKPfznSWaEz7yAyumRcru3wzrjbFPc-XEW_B7M6wTT0U_rCKLj_56_qMij1SaCuduNDLDwbP7nYEupASBe-ELVTD714CwGQvpPxG0muohFrI",
                    name = machine.name ?: "",
                    serial = (machine.counter ?: 0).toString(),
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }
        } else {
            Text(
                text = "Sin máquinas",
                color = textPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.padding(6.dp))

        // Notes Header
        Text(
            text = "Notas",
            color = textPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Notes Text
        if (bar.description.isEmpty()) {
            Text(
                text = "Sin notas",
                color = textPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        } else {
            Text(
                text = bar.description,
                color = textPrimary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun MachineItem(
    imageUrl: String,
    name: String,
    serial: String,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 2.dp)
            .heightIn(min = 72.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
      /*  AsyncImage(
            model = imageUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        )
*/

        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = name,
            modifier = Modifier
                .size(48.dp)
                .background(Color.Gray, RoundedCornerShape(8.dp)),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = name,
                color = textPrimary,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = serial,
                color = textSecondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
