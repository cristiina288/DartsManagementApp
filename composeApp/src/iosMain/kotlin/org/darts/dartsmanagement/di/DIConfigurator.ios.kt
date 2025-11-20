package org.darts.dartsmanagement.di

import org.darts.dartsmanagement.data.auth.ExpectedFirebaseAuth
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore
import org.koin.core.module.Module
import org.koin.dsl.module

/*actual fun platformModule(): Module {
    return module {
        
    }
}*/


actual val platformModule = module {
    single { ExpectedFirestore().apply { /* No actual FirebaseFirestore for iOS stub */ } }
}
