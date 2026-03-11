package org.darts.dartsmanagement.domain.bars

class UpdateBarWithNewMachine(
    private val barsRepository: BarsRepository
) {
    suspend operator fun invoke(barId: String, newMachineId: String) {
        // First, get the current bar to retrieve its existing machineIds
        val bar = barsRepository.getBar(barId).getOrNull()

        // Ensure we have a bar and its machineIds
        if (bar != null) {
            val currentMachineIds = bar.machines.mapNotNull { it.id }.toMutableList()

            // Add the new machineId if it's not already present
            // Convert String machineId to Int for the machineIds list (as per Firestore model)
            if (newMachineId != null && !currentMachineIds.contains(newMachineId)) {
                currentMachineIds.add(newMachineId)
                barsRepository.updateBarMachines(barId, currentMachineIds)
            }
        }
    }
}
