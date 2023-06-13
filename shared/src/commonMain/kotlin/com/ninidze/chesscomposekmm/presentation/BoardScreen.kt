package com.ninidze.chesscomposekmm.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.presentation.ChessBoardEvents.OnGameRestart
import com.ninidze.chesscomposekmm.presentation.ChessBoardEvents.OnPieceMove
import com.ninidze.chesscomposekmm.presentation.component.PieceView
import com.ninidze.chesscomposekmm.presentation.theme.ActiveCell
import com.ninidze.chesscomposekmm.presentation.theme.BackgroundColor
import com.ninidze.chesscomposekmm.presentation.theme.BoardBlack
import com.ninidze.chesscomposekmm.presentation.theme.BoardWhite
import com.ninidze.chesscomposekmm.util.extensions.isOccupiedByOpponent

@Composable
fun BoardScreen(
    chessBoardState: ChessBoard,
    chessBoardEvents: (ChessBoardEvents) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BackgroundColor),
        contentAlignment = Alignment.Center,

        ) {

        var selectedPiece by remember { mutableStateOf<ChessPiece?>(null) }

        if (chessBoardState.winner != null) {
            chessBoardEvents.invoke(OnGameRestart)
        }
        Column {
            chessBoardState.cells.forEachIndexed { rowIndex, row ->
                Row {
                    row.forEachIndexed { columnIndex, piece ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            chessBoardState.SelectPeace(
                                row = rowIndex,
                                column = columnIndex,
                                selectedPiece = selectedPiece
                            )

                            selectedPiece?.DrawAvailableMoves(
                                row = rowIndex,
                                column = columnIndex,
                                chessBoard = chessBoardState,
                                onPieceMove = { piece, position ->
                                    chessBoardEvents(OnPieceMove(piece, position))
                                    selectedPiece = null
                                }
                            )

                            println(piece)
                            piece?.let {
                                PieceView(
                                    piece = it,
                                    onPieceClick = {
                                        if (selectedPiece != null && it.color != selectedPiece?.color) {
                                            chessBoardEvents(
                                                OnPieceMove(
                                                    selectedPiece!!,
                                                    it.position
                                                )
                                            )
                                            selectedPiece = null
                                        } else if (it.color == chessBoardState.playerTurn) {
                                            selectedPiece = it
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChessBoard.SelectPeace(
    row: Int,
    column: Int,
    selectedPiece: ChessPiece?
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canBeCaptured = selectedPiece?.let {
            (isOccupiedByOpponent(
                position = Position(row, column),
                color = it.color
            ) && selectedPiece.getAvailableMoves(this@SelectPeace).contains(Position(row, column)))
        }
        val isCorrectTurn = playerTurn != selectedPiece?.color
        val cellColor = when {
            Position(
                row = row,
                column = column
            ) == selectedPiece?.position || (canBeCaptured == true && !isCorrectTurn) -> ActiveCell

            (row % 2 == 0) == (column % 2 == 0) -> BoardWhite
            else -> BoardBlack
        }
        drawRect(color = cellColor)
    }

}

@Composable
private fun ChessPiece.DrawAvailableMoves(
    row: Int,
    column: Int,
    chessBoard: ChessBoard,
    onPieceMove: (ChessPiece, Position) -> Unit
) {
    if (Position(row, column) !in getAvailableMoves(chessBoard)) return
    if (chessBoard.playerTurn != color) return
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                onPieceMove(this, Position(row, column))
            }
    ) {
        drawCircle(
            color = ActiveCell,
            radius = size.minDimension / 6,
            center = center
        )
    }
}
