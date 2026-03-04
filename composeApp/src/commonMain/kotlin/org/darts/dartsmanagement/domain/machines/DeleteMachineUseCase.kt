package org.darts.dartsmanagement.domain.machines

import org.darts.dartsmanagement.domain.bars.BarsRepository

class DeleteMachineUseCase(
    private val machinesRepository: MachinesRepository,
    private val barsRepository: BarsRepository
) {
    suspend operator fun invoke(machineId: Int): Result<Unit> {
        return runCatching {
            // 1. Get machine details to check for assigned bar
            val machineResult = machinesRepository.getMachine(machineId)
            val barId = machineResult.getOrNull()?.barId

            // 2. Delete the machine
            machinesRepository.deleteMachine(machineId).getOrThrow()

            // 3. If it was in a bar, disassociate it
            if (!barId.isNullOrBlank()) {
                barsRepository.getBar(barId).onSuccess { bar ->
                    val updatedMachineIds = bar.machines.mapNotNull { it.id }.filter { it != machineId }
                    barsRepository.updateBarMachines(barId, updatedMachineIds)
                }
            }
        }
    }
}
