package org.darts.dartsmanagement.domain.machines

import org.darts.dartsmanagement.data.machines.requests.SaveMachineRequest
import org.darts.dartsmanagement.domain.machines.model.MachineModel

interface MachinesRepository {

    suspend fun getMachines(): List<MachineModel>

    suspend fun getMachine(machineId: String): Result<MachineModel>

    suspend fun saveMachine(serialNumber: String, saveMachineRequest: SaveMachineRequest)

    suspend fun updateMachineStatus(machineId: String, statusId: Int): Result<Unit>

    suspend fun updateMachine(machine: MachineModel)

    suspend fun deleteMachine(machineId: String): Result<Unit>

}