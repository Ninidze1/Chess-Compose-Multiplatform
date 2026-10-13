package com.ninidze.chesscomposekmm.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninidze.chesscomposekmm.presentation.ChessBoardEvents.AiMoveFinished
import com.ninidze.chesscomposekmm.presentation.ChessBoardEvents.OnGameRestart
import com.ninidze.chesscomposekmm.presentation.ChessBoardEvents.OnPieceMove
import com.ninidze.chesscomposekmm.presentation.component.GameResultText
import com.ninidze.chesscomposekmm.presentation.component.RenderChessBoard
import com.ninidze.chesscomposekmm.presentation.component.RestartButton
import com.ninidze.chesscomposekmm.presentation.theme.*
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BoardScreen(
    uiState: ChessUiState,
    chessBoardEvents: (ChessBoardEvents) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RenderChessBoard(
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
            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.board.isGameOver && !uiState.isLoading) {
                GameResultText(result = uiState.board.result)
                Spacer(modifier = Modifier.height(16.dp))
            }

            RestartButton(
                isVisible = uiState.board.isGameOver && !uiState.isLoading,
                onRestartClick = { chessBoardEvents(OnGameRestart) }
            )
        }
    }
}
