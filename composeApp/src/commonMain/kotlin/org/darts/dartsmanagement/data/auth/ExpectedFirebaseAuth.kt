package org.darts.dartsmanagement.data.auth

expect class ExpectedFirebaseAuth {
    suspend fun signInWithEmailAndPassword(email: String, password: String)
    fun getCurrentUser(): Any?
    fun signOut()
}
