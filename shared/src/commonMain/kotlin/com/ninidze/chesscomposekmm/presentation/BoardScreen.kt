package com.ninidze.chesscomposekmm.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.presentation.ChessBoardEvents.OnGameRestart
import com.ninidze.chesscomposekmm.presentation.ChessBoardEvents.OnPieceMove
import com.ninidze.chesscomposekmm.presentation.component.RenderChessBoard
import com.ninidze.chesscomposekmm.presentation.component.RestartButton
import com.ninidze.chesscomposekmm.presentation.theme.*
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BoardScreen(
    chessBoardState: ChessBoard,
    chessBoardEvents: (ChessBoardEvents) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        val selectedPiece = remember { mutableStateOf<ChessPiece?>(null) }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Render the chess board and its pieces
            RenderChessBoard(chessBoardState, selectedPiece)
            onPieceMove@ { piece, position ->
                chessBoardEvents(OnPieceMove(piece, position))
                selectedPiece.value = null
            }
            Spacer(modifier = Modifier.height(24.dp))

            RestartButton(
                isVisible = chessBoardState.winner != null,
                onRestartClick = { chessBoardEvents(OnGameRestart) }
            )
        }
    }
}