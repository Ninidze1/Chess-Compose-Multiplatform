package com.ninidze.chesscomposekmm.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.presentation.theme.ActiveCell
import com.ninidze.chesscomposekmm.presentation.theme.BoardBlack
import com.ninidze.chesscomposekmm.presentation.theme.BoardWhite
import com.ninidze.chesscomposekmm.util.extensions.isOccupiedByOpponent

@Composable
fun RenderChessBoard(
    chessBoardState: ChessBoard,
    selectedPiece: MutableState<ChessPiece?>,
    onPieceMove: (ChessPiece, Position) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        chessBoardState.cells.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { columnIndex, piece ->
                    val currentPosition = Position(rowIndex, columnIndex)
                    val cellColor =
                        determineCellColor(selectedPiece.value, currentPosition, chessBoardState)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(cellColor),
                        contentAlignment = Alignment.Center
                    ) {
                        DrawAvailableMoves(
                            selectedPiece = selectedPiece.value,
                            currentPosition = currentPosition,
                            chessBoard = chessBoardState,
                            onPieceMove = onPieceMove
                        )

                        piece?.let { currentPiece ->
                            PieceView(
                                piece = currentPiece,
                                onPieceClick = {
                                    handlePieceClick(
                                        selectedPiece = selectedPiece,
                                        chessBoardState = chessBoardState,
                                        currentPiece = currentPiece,
                                        onPieceMove = onPieceMove
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun determineCellColor(
    selectedPiece: ChessPiece?,
    currentPosition: Position,
    chessBoard: ChessBoard
): Color = with(chessBoard) {
    val isPieceCapturable = selectedPiece?.let { piece ->
        isOccupiedByOpponent(currentPosition, piece.color) && piece.getAvailableMoves(this)
            .contains(currentPosition)
    } ?: false

    val isTurnIncorrect = playerTurn != selectedPiece?.color
    val isSelectedOrCanBeCaptured =
        currentPosition == selectedPiece?.position || (isPieceCapturable && !isTurnIncorrect)
    val isCellWhite = (currentPosition.row % 2 == 0) == (currentPosition.column % 2 == 0)

    return when {
        isSelectedOrCanBeCaptured -> ActiveCell
        isCellWhite -> BoardWhite
        else -> BoardBlack
    }
}

private fun handlePieceClick(
    selectedPiece: MutableState<ChessPiece?>,
    chessBoardState: ChessBoard,
    currentPiece: ChessPiece,
    onPieceMove: (ChessPiece, Position) -> Unit
) {
    if (selectedPiece.value != null && currentPiece.color != selectedPiece.value?.color) {
        selectedPiece.value?.let { onPieceMove(it, currentPiece.position) }
        selectedPiece.value = null
    } else if (currentPiece.color == chessBoardState.playerTurn) {
        selectedPiece.value = currentPiece
    }
}