package org.darts.dartsmanagement.data.machines

import org.darts.dartsmanagement.data.machines.requests.SaveMachineRequest
import org.darts.dartsmanagement.domain.machines.MachinesRepository
import org.darts.dartsmanagement.domain.machines.model.MachineModel

class MachinesRepositoryImpl(private val api: MachinesApiService): MachinesRepository {

    override suspend fun getMachines(): List<MachineModel> {
        return api.getMachines().map { m -> m.toDomain() }
    }


    override suspend fun saveMachine(saveMachineRequest: SaveMachineRequest) {
        return api.saveMachine(saveMachineRequest)
    }
}