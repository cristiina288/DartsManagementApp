package org.darts.dartsmanagement.data.machines

import org.darts.dartsmanagement.data.machines.requests.SaveMachineRequest
import org.darts.dartsmanagement.domain.machines.MachinesRepository
import org.darts.dartsmanagement.domain.machines.model.MachineModel

import org.darts.dartsmanagement.domain.bars.BarsRepository

class MachinesRepositoryImpl(
    private val api: MachinesApiService,
    private val barsRepository: BarsRepository
): MachinesRepository {

    override suspend fun getMachines(): List<MachineModel> {
        return api.getMachines().map { m -> m.toDomain() }
    }

    override suspend fun getMachine(machineId: Int): Result<MachineModel> {
        return runCatching {
            api.getMachine(machineId).toDomain()
        }
    }


    override suspend fun saveMachine(serialNumber: String, saveMachineRequest: SaveMachineRequest) {
        return api.saveMachine(serialNumber, saveMachineRequest)
    }

    override suspend fun updateMachineStatus(machineId: Int, statusId: Int): Result<Unit> {
        return runCatching {
            api.updateMachineStatus(machineId, statusId)
        }
    }

    override suspend fun updateMachine(machine: MachineModel) {
        return api.updateMachine(machine)
    }

    override suspend fun deleteMachine(machineId: Int): Result<Unit> {
        return runCatching {
            // 1. Get machine details to check for assigned bar
            val machine = api.getMachine(machineId).toDomain()
            val barId = machine.barId

            // 2. Delete the machine
            api.deleteMachine(machineId)

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