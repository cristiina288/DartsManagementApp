package org.darts.dartsmanagement.domain.machines

import org.darts.dartsmanagement.domain.machines.model.MachineModel

class GetMachines (private val machinesRepository: MachinesRepository) {

    suspend operator fun invoke(): List<MachineModel> {
        return machinesRepository.getMachines()
    }
}
