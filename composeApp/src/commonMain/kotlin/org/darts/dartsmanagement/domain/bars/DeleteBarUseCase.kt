package org.darts.dartsmanagement.domain.bars

class DeleteBarUseCase(
    private val barsRepository: BarsRepository
) {
    suspend operator fun invoke(barId: String): Result<Unit> {
        return barsRepository.deleteBar(barId)
    }
}
