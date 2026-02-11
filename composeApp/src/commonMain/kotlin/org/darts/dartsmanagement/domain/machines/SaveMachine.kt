package org.darts.dartsmanagement.domain.machines

import org.darts.dartsmanagement.data.machines.requests.SaveMachineRequest

class SaveMachine (val machinesRepository: MachinesRepository) {

    suspend operator fun invoke(serialNumber: String, saveMachineRequest: SaveMachineRequest) {
        return machinesRepository.saveMachine(serialNumber, saveMachineRequest)
    }
}
