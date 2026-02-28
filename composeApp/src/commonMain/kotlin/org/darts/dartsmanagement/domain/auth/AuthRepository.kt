package org.darts.dartsmanagement.domain.auth

import dev.gitlive.firebase.auth.FirebaseUser
import org.darts.dartsmanagement.data.common.firestore.LicenseFirestore
import org.darts.dartsmanagement.data.common.firestore.UserFirestore

interface AuthRepository {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Boolean
    fun getCurrentUser(): FirebaseUser? // Represents FirebaseUser from GitLive
    suspend fun signOut()
    
    suspend fun getUserByEmail(email: String): Result<UserFirestore?>
    suspend fun getLicense(licenseId: String): Result<LicenseFirestore?>
}