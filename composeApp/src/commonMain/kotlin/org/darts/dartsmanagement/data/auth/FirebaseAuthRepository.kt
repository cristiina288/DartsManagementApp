package org.darts.dartsmanagement.data.auth

import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import org.darts.dartsmanagement.domain.auth.AuthRepository

class FirebaseAuthRepository(private val firebaseAuth: FirebaseAuth) : AuthRepository {

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Boolean {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}