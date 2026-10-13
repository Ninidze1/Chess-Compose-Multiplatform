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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.presentation.theme.ActiveCell
import com.ninidze.chesscomposekmm.presentation.theme.BoardBlack
import com.ninidze.chesscomposekmm.presentation.theme.BoardWhite
import com.ninidze.chesscomposekmm.util.Constants.ANIMATION_DURATION_MS
import com.ninidze.chesscomposekmm.util.extensions.isOccupiedByOpponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Renders the board and reports moves by position.
 *
 * @param isInputLocked When true, pieces cannot be selected or moved (AI pending, loading, game over).
 * @param onPieceMove Called with (from, to) once the user's move has been animated.
 * @param onAIMoveFinished Called with the [aiMoveSet] (from, to) once it has been animated.
 */
@Composable
fun RenderChessBoard(
    chessBoardState: ChessBoard,
    aiMoveSet: Pair<Position, Position>?,
    isInputLocked: Boolean,
    composableScope: CoroutineScope = rememberCoroutineScope(),
    onPieceMove: (Position, Position) -> Unit,
    onAIMoveFinished: (Position, Position) -> Unit
) {
    var cellSize by remember { mutableStateOf(IntSize(0, 0)) }
    // The selection is a square, resolved on the current board, and is dropped when the board changes.
    var selectedPosition by remember(chessBoardState) { mutableStateOf<Position?>(null) }
    var pieceOffsets by remember { mutableStateOf<Map<ChessPiece, IntOffset>>(emptyMap()) }
    // True while the user's own move is being animated, before it is sent to the ViewModel.
    var isAnimatingMove by remember { mutableStateOf(false) }

    val currentBoard by rememberUpdatedState(chessBoardState)
    val currentOnPieceMove by rememberUpdatedState(onPieceMove)
    val currentOnAIMoveFinished by rememberUpdatedState(onAIMoveFinished)

    val isLocked = isInputLocked || isAnimatingMove
    val selectedPiece: ChessPiece? = if (isLocked) {
        null
    } else {
        selectedPosition
            ?.let { chessBoardState.getPieceAtPosition(it) }
            ?.takeIf { it.color == chessBoardState.playerTurn }
    }

    // Runs once per published AI move.
    LaunchedEffect(aiMoveSet) {
        val aiMove = aiMoveSet ?: return@LaunchedEffect
        chessBoardState.getPieceAtPosition(aiMove.first)?.let { piece ->
            pieceOffsets = pieceOffsets + (piece to piece.calculateMoveAnimation(
                targetPosition = aiMove.second,
                cellSize = cellSize
            ))
            delay(ANIMATION_DURATION_MS)
        }
        currentOnAIMoveFinished(aiMove.first, aiMove.second)
    }

    val startMove: (ChessPiece, Position) -> Unit = startMove@{ piece, targetPosition ->
        if (isInputLocked || isAnimatingMove) return@startMove
        val boardAtStart = chessBoardState
        val fromPosition = piece.position
        isAnimatingMove = true
        selectedPosition = null
        pieceOffsets = pieceOffsets + (piece to piece.calculateMoveAnimation(
            targetPosition = targetPosition,
            cellSize = cellSize
        ))
        composableScope.launch {
            try {
                delay(ANIMATION_DURATION_MS)
                // Drop the move if the board was replaced (restart, load) meanwhile.
                if (currentBoard === boardAtStart) {
                    currentOnPieceMove(fromPosition, targetPosition)
                }
            } finally {
                isAnimatingMove = false
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
                            selectedPiece, currentPosition, chessBoardState
                        )
                        Box(modifier = Modifier.weight(1f)
                            .onSizeChanged { if (cellSize.width == 0) cellSize = it }
                            .aspectRatio(1f).background(cellColor),
                            contentAlignment = Alignment.Center) {
                            DrawAvailableMoves(selectedPiece = selectedPiece,
                                currentPosition = currentPosition,
                                chessBoard = chessBoardState,
                                onPieceMove = startMove)
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
                                        if (isInputLocked || isAnimatingMove) return@PieceView
                                        handlePieceClick(
                                            selectedPiece = selectedPiece,
                                            chessBoardState = chessBoardState,
                                            currentPiece = currentPiece,
                                            onSelect = { selectedPosition = it },
                                            onPieceMove = startMove
                                        )
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
    selectedPiece: ChessPiece?,
    chessBoardState: ChessBoard,
    currentPiece: ChessPiece,
    onSelect: (Position) -> Unit,
    onPieceMove: (ChessPiece, Position) -> Unit
) {
    val isCapturable = selectedPiece?.let { piece ->
        chessBoardState.isOccupiedByOpponent(currentPiece.position, piece.color) &&
                piece.getAvailableMoves(chessBoardState).contains(currentPiece.position)
    } ?: false
    if (selectedPiece != null && isCapturable) {
        onPieceMove(selectedPiece, currentPiece.position)
    } else if (currentPiece.color == chessBoardState.playerTurn) {
        onSelect(currentPiece.position)
    }
}
