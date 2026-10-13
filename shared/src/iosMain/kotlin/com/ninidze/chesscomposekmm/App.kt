package com.ninidze.chesscomposekmm

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import com.ninidze.chesscomposekmm.presentation.BoardScreen
import com.ninidze.chesscomposekmm.presentation.ChessViewModel
import com.ninidze.chesscomposekmm.presentation.theme.ChessAppTheme
import org.koin.mp.KoinPlatform

// The iOS app is a single screen that lives as long as the process, so it keeps one
// ChessViewModel (with its own scope) as an app-lifetime singleton, created on first use
// after initKoin(); it is never cleared because nothing outlives it.
private val chessViewModel: ChessViewModel by lazy { KoinPlatform.getKoin().get() }

// Called from Swift as AppKt.MainViewController(), so it keeps the UIKit factory name.
@Suppress("ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController {
    val uiState by chessViewModel.uiState.collectAsState()
    ChessAppTheme {
        BoardScreen(
            uiState = uiState,
            chessBoardEvents = chessViewModel::onEvent
        )
    }
}
