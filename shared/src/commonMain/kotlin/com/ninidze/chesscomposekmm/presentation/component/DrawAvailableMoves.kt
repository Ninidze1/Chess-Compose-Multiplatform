package com.ninidze.chesscomposekmm.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.presentation.theme.ActiveCell

@Composable
fun DrawAvailableMoves(
    selectedPiece: ChessPiece?,
    currentPosition: Position,
    chessBoard: ChessBoard,
    onPieceMove: (ChessPiece, Position) -> Unit
) {
    selectedPiece?.let { piece ->
        if (currentPosition !in piece.getAvailableMoves(chessBoard)) return
        if (chessBoard.playerTurn != piece.color) return


        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    onPieceMove(piece, currentPosition)
                }
        ) {
            drawCircle(
                color = ActiveCell,
                radius = size.minDimension / 6,
                center = center
            )
        }
    }
}