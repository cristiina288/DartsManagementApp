package org.darts.dartsmanagement.domain.machines

class UpdateMachineStatusUseCase(
    private val machinesRepository: MachinesRepository
) {
    suspend operator fun invoke(machineId: Int, statusId: Int): Result<Unit> {
        return machinesRepository.updateMachineStatus(machineId, statusId)
    }
}
