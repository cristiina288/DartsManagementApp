package org.darts.dartsmanagement.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.darts.dartsmanagement.data.auth.ExpectedFirebaseAuth
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore
import org.koin.dsl.module

actual val platformModule = module {
    single { FirebaseAuth.getInstance() }
    single { ExpectedFirebaseAuth(get()) }
    single { FirebaseFirestore.getInstance() }
    single { ExpectedFirestore(get()) }
}
