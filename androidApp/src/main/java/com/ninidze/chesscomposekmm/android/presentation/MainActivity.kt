package com.ninidze.chesscomposekmm.android.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ninidze.chesscomposekmm.presentation.BoardScreen
import com.ninidze.chesscomposekmm.presentation.theme.ChessAppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AndroidChessViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // The same theme as iOS; it follows the system dark mode setting.
            ChessAppTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                BoardScreen(
                    uiState = uiState,
                    chessBoardEvents = viewModel::onEvent
                )
            }
        }
    }
}
