package org.darts.dartsmanagement.domain.machines

class UpdateMachineStatusUseCase(
    private val machinesRepository: MachinesRepository
) {
    suspend operator fun invoke(machineId: String, statusId: Int): Result<Unit> {
        return machinesRepository.updateMachineStatus(machineId, statusId)
    }
}
