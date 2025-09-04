package org.darts.dartsmanagement


import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.darts.dartsmanagement.ui.home.HomeScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import org.darts.dartsmanagement.ui.auth.AuthScreen
import org.darts.dartsmanagement.ui.core.Routes
import org.darts.dartsmanagement.ui.home.collections.CollectionScreen

@Composable
@Preview
fun App() {
    MaterialTheme {

        //val navController = rememberNavController()
        var startDestination by remember { mutableStateOf(Routes.Auth.route) }

        // Verificar si hay un usuario autenticado al inicio
        LaunchedEffect(Unit) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            startDestination = if (currentUser != null) Routes.Home.route else Routes.Auth.route
        }

        if (startDestination == Routes.Home.route) {
            Navigator(screen = HomeScreen)
        } else {
            Navigator(screen = AuthScreen )
        }
    }
}