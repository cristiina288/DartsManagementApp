package org.darts.dartsmanagement.domain.bars

class UpdateBarWithNewMachine(
    private val barsRepository: BarsRepository
) {
    suspend operator fun invoke(barId: String, newMachineId: String) {
        // First, get the current bar to retrieve its existing machine_ids
        val bar = barsRepository.getBar(barId).getOrNull()

        // Ensure we have a bar and its machine_ids
        if (bar != null) {
            val currentMachineIds = bar.machines.mapNotNull { it.id }.toMutableList()

            // Add the new machineId if it's not already present
            // Convert String machineId to Int for the machine_ids list (as per Firestore model)
            val newMachineIdInt = newMachineId.toIntOrNull()
            if (newMachineIdInt != null && !currentMachineIds.contains(newMachineIdInt)) {
                currentMachineIds.add(newMachineIdInt)
                barsRepository.updateBarMachines(barId, currentMachineIds)
            }
        }
    }
}
