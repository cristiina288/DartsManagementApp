package org.darts.dartsmanagement.ui.home.machines.detail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
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
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.domain.machines.model.MachineModel

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.viewmodel.koinViewModel

class MachineScreen (val machine: MachineModel) : Screen {
    @Composable
    override fun Content() {
        MachineScreenContent(machine)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineScreenContent(
    machine: MachineModel,
    barName: String = "Bar A",
    address: String = "123 Main St, Anytown",
    totalAmount: String = "150 €",
    collections: List<CollectionModel> = emptyList(),
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    val navigator = LocalNavigator.currentOrThrow

    val machineViewModel = koinViewModel<MachineViewModel>()

    val collections by machineViewModel.collections.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2832))
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
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
                text = "Machine Details",
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Machine Image and Info Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Machine Image (Circular)
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4A5D6B)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Placeholder for machine image
                        // You can replace this with AsyncImage or Image composable
                        Icon(
                            imageVector = Icons.Default.ShoppingCart, // Replace with actual machine icon
                            contentDescription = "Machine Image",
                            tint = Color(0xFF8B9CAD),
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Machine Name
                    Text(
                        text = machine.name ?: "",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Assigned to Bar
                    Text(
                        text = "Situada en $barName",
                        color = Color(0xFF8B9CAD),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Address
                    Text(
                        text = address,
                        color = Color(0xFF8B9CAD),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Total Collections Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2D3748)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Total Collections on $barName",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = totalAmount,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Collection History Title
                Text(
                    text = "Collection History",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Collection History Items
            itemsIndexed(collections ?: emptyList()) { index, collection ->
                CollectionHistoryItem(
                    collection = collection,
                    item = index
                )
            }
        }
    }
}

@Composable
fun CollectionHistoryItem(
    collection: CollectionModel,
    item: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Collection Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF4A5568)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle, // Replace with actual collection icon
                contentDescription = "Collection",
                tint = Color(0xFF8B9CAD),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Collection Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Recaudación $item",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            val totalAmount = collection.collectionAmounts.totalCollection

            Text(
                text = "$totalAmount €",
                color = Color(0xFF8B9CAD),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Amount (if needed, currently not shown in the design)
        // Text(
        //     text = collection.amount,
        //     color = Color.White,
        //     fontSize = 16.sp,
        //     fontWeight = FontWeight.Medium
        // )
    }
}