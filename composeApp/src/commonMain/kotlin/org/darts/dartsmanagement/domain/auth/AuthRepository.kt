package org.darts.dartsmanagement.domain.auth

import dev.gitlive.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Boolean
    fun getCurrentUser(): FirebaseUser? // Represents FirebaseUser from GitLive
    suspend fun signOut()
}