package org.darts.dartsmanagement.domain.machines

class DeleteMachineUseCase(
    private val machinesRepository: MachinesRepository
) {
    suspend operator fun invoke(machineId: Int): Result<Unit> {
        return machinesRepository.deleteMachine(machineId)
    }
}
