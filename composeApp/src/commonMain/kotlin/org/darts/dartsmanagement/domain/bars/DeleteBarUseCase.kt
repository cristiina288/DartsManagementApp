package org.darts.dartsmanagement.domain.bars

import org.darts.dartsmanagement.domain.machines.MachinesRepository

class DeleteBarUseCase(
    private val barsRepository: BarsRepository,
    private val machinesRepository: MachinesRepository
) {
    suspend operator fun invoke(barId: String): Result<Unit> {
        return runCatching {
            // 1. Get bar details to check for assigned machines
            val barResult = barsRepository.getBar(barId)
            val machineIds = barResult.getOrNull()?.machines?.mapNotNull { it.id } ?: emptyList()

            // 2. Delete the bar
            barsRepository.deleteBar(barId).getOrThrow()

            // 3. If it had machines, disassociate them
            machineIds.forEach { machineId ->
                machinesRepository.getMachine(machineId).onSuccess { machine ->
                    val updatedMachine = machine.copy(barId = "")
                    machinesRepository.updateMachine(updatedMachine)
                }
            }
        }
    }
}
