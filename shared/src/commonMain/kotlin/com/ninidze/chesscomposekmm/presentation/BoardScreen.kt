package com.ninidze.chesscomposekmm.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

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
        contentAlignment = Alignment.Center,

        ) {

        var selectedPiece by remember { mutableStateOf<ChessPiece?>(null) }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            chessBoardState.cells.forEachIndexed { rowIndex, row ->
                Row {
                    row.forEachIndexed { columnIndex, piece ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            SelectPiece(
                                row = rowIndex,
                                column = columnIndex,
                                chessBoard = chessBoardState,
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
            Spacer(modifier = Modifier.height(20.dp))
            AnimatedVisibility(visible = chessBoardState.winner != null) {
                Button(
                    modifier = Modifier
                        .height(75.dp)
                        .aspectRatio(1f),
                    onClick = {
                        chessBoardEvents(OnGameRestart)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BoardWhite
                    )
                ) {
                    Image(
                        painter = painterResource(res = "drawables/ic_restart.xml"),
                        contentDescription = "Restart"
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectPiece(
    row: Int,
    column: Int,
    chessBoard: ChessBoard,
    selectedPiece: ChessPiece?
) = with(chessBoard) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val currentPosition = Position(row, column)
        val isPieceCapturable = isPieceCapturable(selectedPiece, currentPosition)
        val isTurnIncorrect = playerTurn != selectedPiece?.color

        val cellColor = determineCellColor(
            selectedPiece = selectedPiece,
            currentPosition = currentPosition,
            isPieceCapturable = isPieceCapturable,
            isTurnIncorrect = isTurnIncorrect
        )

        drawRect(color = cellColor)
    }
}

private fun ChessBoard.isPieceCapturable(selectedPiece: ChessPiece?, position: Position) =
    selectedPiece?.let { piece ->
        isOccupiedByOpponent(position, piece.color) && piece.getAvailableMoves(this)
            .contains(position)
    } ?: false

private fun determineCellColor(
    selectedPiece: ChessPiece?,
    currentPosition: Position,
    isPieceCapturable: Boolean,
    isTurnIncorrect: Boolean
): Color {
    val isSelectedOrCanBeCaptured =
        currentPosition == selectedPiece?.position || (isPieceCapturable && !isTurnIncorrect)
    val isCellWhite = (currentPosition.row % 2 == 0) == (currentPosition.column % 2 == 0)
    return when {
        isSelectedOrCanBeCaptured -> ActiveCell
        isCellWhite -> BoardWhite
        else -> BoardBlack
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
