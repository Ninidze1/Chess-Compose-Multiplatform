package com.ninidze.chesscomposekmm.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninidze.chesscomposekmm.presentation.ChessBoardEvents.AiMoveFinished
import com.ninidze.chesscomposekmm.presentation.ChessBoardEvents.OnGameRestart
import com.ninidze.chesscomposekmm.presentation.ChessBoardEvents.OnPieceMove
import com.ninidze.chesscomposekmm.presentation.component.GameResultText
import com.ninidze.chesscomposekmm.presentation.component.RenderChessBoard
import com.ninidze.chesscomposekmm.presentation.component.RestartButton
import com.ninidze.chesscomposekmm.presentation.component.WinningDialog

/**
 * The game screen. The board is the largest square that fits next to the controls: below
 * them in portrait, beside them in landscape, so the restart button always stays visible.
 */
@Composable
fun BoardScreen(
    uiState: ChessUiState,
    chessBoardEvents: (ChessBoardEvents) -> Unit
) {
    val result = uiState.board.result
    val isFinished = uiState.board.isGameOver && !uiState.isLoading
    // Shown again for every newly finished game, since the result passes through Ongoing.
    var isResultDialogDismissed by remember(result) { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            // Both platforms draw edge to edge; keep the board and controls clear of the
            // status bar, notch and navigation bar.
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        if (maxWidth > maxHeight) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(LANDSCAPE_PADDING.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Board(
                    modifier = Modifier.weight(1f, fill = false),
                    uiState = uiState,
                    chessBoardEvents = chessBoardEvents
                )
                Spacer(modifier = Modifier.width(CONTROLS_SPACING.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GameControls(
                        isFinished = isFinished,
                        uiState = uiState,
                        chessBoardEvents = chessBoardEvents
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Board(
                    modifier = Modifier.weight(1f, fill = false),
                    uiState = uiState,
                    chessBoardEvents = chessBoardEvents
                )
                Spacer(modifier = Modifier.height(CONTROLS_SPACING.dp))
                GameControls(
                    isFinished = isFinished,
                    uiState = uiState,
                    chessBoardEvents = chessBoardEvents
                )
            }
        }

        if (isFinished && !isResultDialogDismissed) {
            WinningDialog(
                modifier = Modifier.fillMaxSize(),
                result = result,
                humanColor = uiState.humanColor,
                onPlayAgainClick = { chessBoardEvents(OnGameRestart) },
                onDismiss = { isResultDialogDismissed = true }
            )
        }
    }
}

@Composable
private fun Board(
    modifier: Modifier,
    uiState: ChessUiState,
    chessBoardEvents: (ChessBoardEvents) -> Unit
) {
    RenderChessBoard(
        modifier = modifier,
        chessBoardState = uiState.board,
        aiMoveSet = uiState.aiMove?.let { it.from to it.to },
        isInputLocked = uiState.isInputLocked,
        onAIMoveFinished = { from, to ->
            chessBoardEvents(AiMoveFinished(from, to))
        },
        onPieceMove = { from, to, promotion ->
            chessBoardEvents(OnPieceMove(from, to, promotion))
        }
    )
}

/** The result of a finished game and the restart button; empty while the game is ongoing. */
@Composable
private fun GameControls(
    isFinished: Boolean,
    uiState: ChessUiState,
    chessBoardEvents: (ChessBoardEvents) -> Unit
) {
    if (isFinished) {
        GameResultText(result = uiState.board.result)
        Spacer(modifier = Modifier.height(16.dp))
    }

    RestartButton(
        isVisible = isFinished,
        onRestartClick = { chessBoardEvents(OnGameRestart) }
    )
}

private const val LANDSCAPE_PADDING = 16
private const val CONTROLS_SPACING = 24
