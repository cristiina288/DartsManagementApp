package org.darts.dartsmanagement


import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import org.darts.dartsmanagement.ui.home.HomeScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

import org.darts.dartsmanagement.ui.home.collections.CollectionScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        Navigator(screen = HomeScreen)
    }
}