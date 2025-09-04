package org.darts.dartsmanagement.ui.core

sealed class Routes (val route: String) {
    data object Home: Routes("home")
    data object Auth: Routes("auth")
}