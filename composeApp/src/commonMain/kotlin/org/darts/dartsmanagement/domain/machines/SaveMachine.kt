package org.darts.dartsmanagement.domain.machines

import org.darts.dartsmanagement.data.machines.requests.SaveMachineRequest

class SaveMachine (val machinesRepository: MachinesRepository) {

    suspend operator fun invoke(saveMachineRequest: SaveMachineRequest) {
        return machinesRepository.saveMachine(saveMachineRequest)
    }
}
