package com.ninidze.chesscomposekmm.presentation.component

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.engine.Piece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceColor.Black
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.presentation.theme.ActiveCell
import com.ninidze.chesscomposekmm.presentation.theme.BoardBlack
import com.ninidze.chesscomposekmm.presentation.theme.BoardWhite
import com.ninidze.chesscomposekmm.util.Constants.ANIMATION_DURATION_MS
import com.ninidze.chesscomposekmm.util.extensions.isOccupiedByOpponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RenderChessBoard(
    chessBoardState: ChessBoard,
    aiMoveSet: Pair<Position, Position>?,
    composableScope: CoroutineScope = rememberCoroutineScope(),
    onPieceMove: (ChessPiece, Position) -> Unit,
    onAIMoveFinished : () -> Unit
) {
    var cellSize by remember { mutableStateOf(IntSize(0, 0)) }
    val selectedPiece = remember { mutableStateOf<ChessPiece?>(null) }
    var pieceOffsets by remember { mutableStateOf<Map<ChessPiece, IntOffset>>(emptyMap()) }

    if (aiMoveSet != null) {
        chessBoardState.getPieceAtPosition(aiMoveSet.first)?.let { piece ->
            val targetPosition = aiMoveSet.second
            pieceOffsets =
                pieceOffsets + (piece to piece.calculateMoveAnimation(
                    targetPosition = targetPosition,
                    cellSize = cellSize
                ))
            composableScope.launch {
                delay(ANIMATION_DURATION_MS)
                onAIMoveFinished.invoke()
            }
        }
    }

    // Render Cells
    Box {
        Column {
            chessBoardState.cells.forEachIndexed { rowIndex, row ->
                Row {
                    row.forEachIndexed { columnIndex, _ ->
                        val currentPosition = Position(rowIndex, columnIndex)
                        val cellColor = determineCellColor(
                            selectedPiece.value, currentPosition, chessBoardState
                        )
                        Box(modifier = Modifier.weight(1f)
                            .onSizeChanged { if (cellSize.width == 0) cellSize = it }
                            .aspectRatio(1f).background(cellColor),
                            contentAlignment = Alignment.Center) {
                            DrawAvailableMoves(selectedPiece = selectedPiece.value,
                                currentPosition = currentPosition,
                                chessBoard = chessBoardState,
                                onPieceMove = { chessPiece, targetPosition ->
                                    pieceOffsets =
                                        pieceOffsets + (chessPiece to chessPiece.calculateMoveAnimation(
                                            targetPosition = targetPosition, cellSize = cellSize
                                        ))
                                    composableScope.launch {
                                        delay(ANIMATION_DURATION_MS)
                                        onPieceMove.invoke(chessPiece, targetPosition)
                                    }
                                    selectedPiece.value = null
                                })
                        }
                    }
                }
            }
        }

        // Render Pieces
        Column {
            chessBoardState.cells.forEachIndexed { _, row ->
                Row {
                    row.forEachIndexed { _, piece ->
                        Box(
                            modifier = Modifier.zIndex(2f).weight(1f).aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            piece?.let { currentPiece ->
                                val targetPosition = pieceOffsets[currentPiece] ?: IntOffset(0, 0)
                                val offsetState by animateIntOffsetAsState(
                                    targetValue = targetPosition,
                                    animationSpec = tween(durationMillis = ANIMATION_DURATION_MS.toInt())
                                )
                                PieceView(
                                    modifier = Modifier
                                        .offset { offsetState }
                                        .fillMaxSize(),
                                    piece = currentPiece,
                                    onPieceClick = {
                                        if (chessBoardState.winner != null) return@PieceView
                                        handlePieceClick(selectedPiece = selectedPiece,
                                            chessBoardState = chessBoardState,
                                            currentPiece = currentPiece,
                                            onPieceMove = Piece@{ targetPosition ->
                                                pieceOffsets =
                                                    pieceOffsets + (this to this.calculateMoveAnimation(
                                                        targetPosition = targetPosition,
                                                        cellSize = cellSize
                                                    ))
                                                composableScope.launch CoroutineScope@{
                                                    delay(ANIMATION_DURATION_MS)
                                                    onPieceMove.invoke(this@Piece, targetPosition)
                                                }
                                            })
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}
private fun ChessPiece.calculateMoveAnimation(
    targetPosition: Position, cellSize: IntSize
) = IntOffset(
    ((targetPosition.column - this.position.column) * cellSize.width),
    ((targetPosition.row - this.position.row) * cellSize.height)
)

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
    onPieceMove: ChessPiece.(Position) -> Unit
) {
    val isCapturable = selectedPiece.value?.let { piece ->
        chessBoardState.isOccupiedByOpponent(currentPiece.position, piece.color) &&
                piece.getAvailableMoves(chessBoardState).contains(currentPiece.position)
    } ?: false
    if (selectedPiece.value != null && currentPiece.color != selectedPiece.value?.color && isCapturable) {
        selectedPiece.value?.let { onPieceMove.invoke(it, currentPiece.position) }
        selectedPiece.value = null
    } else if (currentPiece.color == chessBoardState.playerTurn) {
        selectedPiece.value = currentPiece
    }
}
