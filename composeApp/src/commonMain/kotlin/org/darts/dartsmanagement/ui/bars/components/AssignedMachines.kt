
package org.darts.dartsmanagement.ui.bars.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dartsmanagement.composeapp.generated.resources.Res
import dartsmanagement.composeapp.generated.resources.ico_delete
import org.darts.dartsmanagement.domain.machines.model.MachineModel
import org.darts.dartsmanagement.ui.theme.*
import org.jetbrains.compose.resources.painterResource


@Composable
fun AssignedMachinesSection(
    machines: List<MachineModel>,
    onAddMachineClick: () -> Unit,
    onDeleteMachineClick: (MachineModel) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Máquinas asignadas",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 4.dp),
            fontWeight = FontWeight.Bold
        )

        if (machines.isNotEmpty()) {
            machines.forEach { machine ->
                MachineItem(
                    machine = machine,
                    onDeleteClick = { onDeleteMachineClick(machine) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        AddMachineButton(onClick = onAddMachineClick)
    }
}

@Composable
fun MachineItem(machine: MachineModel, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = machine.name ?: "N/A",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "ID: ${machine.id}",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(Res.drawable.ico_delete),
                    contentDescription = "Delete Machine",
                    modifier = Modifier.size(24.dp),
                    tint = Error
                )
            }
        }
    }
}

@Composable
fun AddMachineButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryAccent.copy(alpha = 0.2f)
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Machine",
            tint = PrimaryAccent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Añadir máquina",
            color = PrimaryAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
