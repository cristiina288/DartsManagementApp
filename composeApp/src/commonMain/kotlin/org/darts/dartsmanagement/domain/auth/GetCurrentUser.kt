package org.darts.dartsmanagement.domain.auth

import dev.gitlive.firebase.auth.FirebaseUser

class GetCurrentUser(private val authRepository: AuthRepository) {

    suspend operator fun invoke(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }
}