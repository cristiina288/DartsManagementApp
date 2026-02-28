package org.darts.dartsmanagement.data.auth

import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import org.darts.dartsmanagement.data.common.firestore.LicenseFirestore
import org.darts.dartsmanagement.data.common.firestore.UserFirestore
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore
import org.darts.dartsmanagement.domain.auth.AuthRepository

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: ExpectedFirestore
) : AuthRepository {

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

    override suspend fun getUserByEmail(email: String): Result<UserFirestore?> {
        return runCatching {
            val docs = firestore.getDocuments("users", "email", email)
            docs.firstOrNull()?.data<UserFirestore>()
        }
    }

    override suspend fun getLicense(licenseId: String): Result<LicenseFirestore?> {
        return runCatching {
            val doc = firestore.getDocument("licenses", licenseId)
            doc?.data<LicenseFirestore>()
        }
    }
}