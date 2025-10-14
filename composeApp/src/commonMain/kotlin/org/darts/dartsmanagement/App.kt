package org.darts.dartsmanagement

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.Navigator
import org.darts.dartsmanagement.ui.home.auth.AuthScreen
import org.darts.dartsmanagement.ui.home.HomeScreen
import org.darts.dartsmanagement.ui.home.HomeViewModel
import org.darts.dartsmanagement.ui.home.collections.CollectionScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val homeViewModel = koinViewModel<HomeViewModel>()
        val isCheckingAuth by homeViewModel.isCheckingAuth.collectAsState()
        val currentUser by homeViewModel.currentUser.collectAsState()

        if (isCheckingAuth) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (currentUser != null) {
                Navigator(screen = HomeScreen)
            } else {
                Navigator(screen = AuthScreen)
            }
        }
    }
}