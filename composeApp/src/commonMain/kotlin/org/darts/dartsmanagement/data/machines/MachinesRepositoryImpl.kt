package org.darts.dartsmanagement.data.machines

import org.darts.dartsmanagement.data.machines.requests.SaveMachineRequest
import org.darts.dartsmanagement.domain.machines.MachinesRepository
import org.darts.dartsmanagement.domain.machines.model.MachineModel

class MachinesRepositoryImpl(
    private val api: MachinesApiService
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
            api.deleteMachine(machineId)
        }
    }
}