package org.darts.dartsmanagement

import androidx.compose.ui.window.ComposeUIViewController
import org.darts.dartsmanagement.di.initKoin

fun MainViewController() = ComposeUIViewController(configure = { initKoin() }) { App() }