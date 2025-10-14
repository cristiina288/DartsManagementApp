package org.darts.dartsmanagement.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

actual class ExpectedFirebaseAuth(private val firebaseAuth: FirebaseAuth) {
    actual suspend fun signInWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    actual fun getCurrentUser(): Any? {
        return firebaseAuth.currentUser
    }

    actual fun signOut() {
        firebaseAuth.signOut()
    }
}
