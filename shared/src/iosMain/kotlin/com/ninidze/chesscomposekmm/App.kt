package com.ninidze.chesscomposekmm

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import com.ninidze.chesscomposekmm.di.inject
import com.ninidze.chesscomposekmm.presentation.BoardScreen
import com.ninidze.chesscomposekmm.presentation.ChessViewModel
import com.ninidze.chesscomposekmm.presentation.theme.ChessAppTheme

fun MainViewController() = ComposeUIViewController {
    val viewModel by inject<ChessViewModel>()
    val state by viewModel.chessBoardState.collectAsState()
    ChessAppTheme {
        BoardScreen(
            chessBoardState = state,
            chessBoardEvents = viewModel::onEvent
        )
    }
}
