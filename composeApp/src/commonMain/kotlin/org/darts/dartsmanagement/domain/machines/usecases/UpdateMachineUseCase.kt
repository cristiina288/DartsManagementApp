package org.darts.dartsmanagement.domain.machines.usecases

import org.darts.dartsmanagement.domain.bars.BarsRepository
import org.darts.dartsmanagement.domain.common.models.Status
import org.darts.dartsmanagement.domain.machines.MachinesRepository
import org.darts.dartsmanagement.domain.machines.model.MachineModel

class UpdateMachineUseCase(
    private val machinesRepository: MachinesRepository,
    private val barsRepository: BarsRepository
) {
    suspend operator fun invoke(updatedMachine: MachineModel, oldBarId: String?) {
        val newBarId = updatedMachine.barId

        // 1. Update machine status based on bar assignment
        val machineToUpdate = updatedMachine.copy(
            status = if (newBarId.isNullOrEmpty()) "INACTIVE" else "ACTIVE"
        )

        // 2. Perform the machine update
        machinesRepository.updateMachine(machineToUpdate)

        // 3. Handle Bar changes
        if (oldBarId != newBarId) {
            val machineIdInt = machineToUpdate.id ?: return

            // Remove from old bar
            if (!oldBarId.isNullOrEmpty()) {
                barsRepository.getBar(oldBarId).onSuccess { oldBar ->
                    val updatedMachineIds = oldBar.machines.mapNotNull { it.id }.filter { it != machineIdInt }
                    barsRepository.updateBarMachines(oldBarId, updatedMachineIds)
                }
            }

            // Add to new bar
            if (!newBarId.isNullOrEmpty()) {
                barsRepository.getBar(newBarId).onSuccess { newBar ->
                    val currentMachineIds = newBar.machines.mapNotNull { it.id }.toMutableList()
                    if (!currentMachineIds.contains(machineIdInt)) {
                        currentMachineIds.add(machineIdInt)
                        barsRepository.updateBarMachines(newBarId, currentMachineIds)
                    }
                }
            }
        }
    }
}
