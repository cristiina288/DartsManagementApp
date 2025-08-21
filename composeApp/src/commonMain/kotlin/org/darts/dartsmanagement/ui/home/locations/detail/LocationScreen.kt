package org.darts.dartsmanagement.ui.home.locations.detail

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.locations.model.LocationModel


class LocationScreen (val location: LocationModel) : Screen {
    @Composable
    override fun Content() {
        LocationScreenContent(location)
    }
}

@Composable
fun LocationScreenContent(
    location: LocationModel,
    locationImageRes: Int? = null, // You can replace this with image URL for AsyncImage
    bars: List<BarModel> = emptyList(),
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onBarClick: (BarModel) -> Unit = {},
    onViewOnMapsClick: (BarModel) -> Unit = {}
) {
    val navigator = LocalNavigator.currentOrThrow

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2832))
    ) {
        item {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navigator.pop() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Location Details",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )

                TextButton(
                    onClick = onEditClick
                ) {
                    Text(
                        text = "Edit",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }

        item {
            // Location Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF4A5D6B)),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder for location image
                // You can replace this with AsyncImage or Image composable
                if (locationImageRes != null) {
                    // Image(
                    //     painter = painterResource(id = locationImageRes),
                    //     contentDescription = "Location Image",
                    //     modifier = Modifier.fillMaxSize(),
                    //     contentScale = ContentScale.Crop
                    // )
                } else {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        // painter = painterResource(id = android.R.drawable.ic_menu_mapmode),
                        contentDescription = "Location Image Placeholder",
                        tint = Color(0xFF8B9CAD),
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }

        item {
            // Location Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Text(
                    text = location.name ?: "",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Postal Code: ${location.postalCode}",
                    color = Color(0xFF8B9CAD),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        item {
            // Bars Section Title
            Text(
                text = "Bars",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Bars List
        items(bars) { bar ->
            BarListItem(
                bar = bar,
                onBarClick = { onBarClick(bar) },
                onViewOnMapsClick = { onViewOnMapsClick(bar) }
            )
        }

        item {
            // Bottom padding
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BarListItem(
    bar: BarModel,
    onBarClick: () -> Unit,
    onViewOnMapsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBarClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bar Image
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF4A5568)),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for bar image
            // You can replace this with AsyncImage or Image composable
           /* if (bar.imageRes != null) {
                // Image(
                //     painter = painterResource(id = bar.imageRes),
                //     contentDescription = "${bar.name} Image",
                //     modifier = Modifier.fillMaxSize(),
                //     contentScale = ContentScale.Crop
                // )
            } else {*/
                Icon(
                    imageVector = Icons.Default.Star,
                    //painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                    contentDescription = "Bar Image Placeholder",
                    tint = Color(0xFF8B9CAD),
                    modifier = Modifier.size(30.dp)
                )
            //}
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Bar Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = bar.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            TextButton(
                onClick = onViewOnMapsClick,
                modifier = Modifier.padding(top = 4.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "View on Maps",
                    color = Color(0xFF8B9CAD),
                    fontSize = 14.sp
                )
            }
        }
    }
}
