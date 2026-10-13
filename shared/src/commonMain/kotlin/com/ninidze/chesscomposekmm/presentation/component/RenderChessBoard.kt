package com.ninidze.chesscomposekmm.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.min
import androidx.compose.ui.zIndex
import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.CastlingRights
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.presentation.Strings
import com.ninidze.chesscomposekmm.presentation.theme.LocalBoardColors
import com.ninidze.chesscomposekmm.util.Constants.ANIMATION_DURATION_MS
import com.ninidze.chesscomposekmm.util.Constants.BOARD_SIZE
import com.ninidze.chesscomposekmm.util.extensions.toAlgebraic
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A move that is being animated on the board.
 *
 * @property promotion The piece a promoting pawn becomes; null for every other move.
 * @property isAiMove True for the AI move published by the ViewModel, false for the user's own move.
 */
private data class AnimatedMove(
    val from: Position,
    val to: Position,
    val promotion: PieceType?,
    val isAiMove: Boolean
)

/**
 * What every affected piece does while a move animates.
 *
 * @property slides Start square to end square of each sliding piece: the mover, plus the
 * rook when castling.
 * @property captured The square of the piece that fades out: the target square for a normal
 * capture, the square beside the pawn for en passant; null when nothing is captured.
 */
private data class MoveMotion(
    val slides: Map<Position, Position>,
    val captured: Position?
)

/**
 * Renders the board as the largest square that fits the incoming constraints, and reports
 * moves by position.
 *
 * Every square is one accessible button that selects a piece or moves the selected piece
 * there. The pieces are drawn in a single layer above the squares, so the moving piece can
 * be raised above all others.
 *
 * A pawn move onto the last rank first shows a [PromotionPicker]; the move is only animated
 * and reported once a piece is chosen, and dismissing the picker cancels it.
 *
 * @param isInputLocked When true, pieces cannot be selected or moved (AI pending, loading, game over).
 * @param onPieceMove Called with (from, to, promotion) once the user's move has been animated.
 * promotion is the chosen piece for a promoting pawn move, null for every other move.
 * @param onAIMoveFinished Called with the [aiMoveSet] (from, to) once it has been animated.
 */
@Composable
fun RenderChessBoard(
    modifier: Modifier = Modifier,
    chessBoardState: ChessBoard,
    aiMoveSet: Pair<Position, Position>?,
    isInputLocked: Boolean,
    onPieceMove: (Position, Position, PieceType?) -> Unit,
    onAIMoveFinished: (Position, Position) -> Unit
) {
    // The selection, the user's move being animated and a pending promotion all belong to
    // one board: they are dropped when the board changes.
    var selectedPosition by remember(chessBoardState) { mutableStateOf<Position?>(null) }
    var userMove by remember(chessBoardState) { mutableStateOf<AnimatedMove?>(null) }
    var pendingPromotion by remember(chessBoardState) {
        mutableStateOf<Pair<ChessPiece, Position>?>(null)
    }

    val currentBoard by rememberUpdatedState(chessBoardState)
    val currentOnPieceMove by rememberUpdatedState(onPieceMove)
    val currentOnAIMoveFinished by rememberUpdatedState(onAIMoveFinished)

    val aiMove = aiMoveSet?.let { (from, to) ->
        AnimatedMove(from = from, to = to, promotion = null, isAiMove = true)
    }
    val activeMove = userMove ?: aiMove

    val isLocked = isInputLocked || userMove != null || pendingPromotion != null
    val selectedPiece: ChessPiece? = if (isLocked) {
        null
    } else {
        selectedPosition
            ?.let { chessBoardState.getPieceAtPosition(it) }
            ?.takeIf { it.color == chessBoardState.playerTurn }
    }

    // Legal-move generation is expensive, so it runs once per board and selection.
    val legalTargets: Set<Position> = remember(chessBoardState, selectedPiece?.position) {
        selectedPiece?.getAvailableMoves(chessBoardState)?.toSet() ?: emptySet()
    }

    // 0 -> 1 progress of the active move; a fresh one starts at 0 for every move.
    val moveProgress = remember(activeMove) { Animatable(0f) }
    val motion: MoveMotion? = remember(activeMove, chessBoardState) {
        activeMove?.let { chessBoardState.motionOf(it) }
    }

    // Runs once per move, outside composition, and reports it when the animation has ended.
    LaunchedEffect(activeMove) {
        val move = activeMove ?: return@LaunchedEffect
        val boardAtStart = chessBoardState
        val animationSpec = tween<Float>(durationMillis = ANIMATION_DURATION_MS.toInt())
        if (move.isAiMove) {
            if (boardAtStart.getPieceAtPosition(move.from) != null) {
                moveProgress.animateTo(targetValue = 1f, animationSpec = animationSpec)
            }
            currentOnAIMoveFinished(move.from, move.to)
        } else {
            try {
                moveProgress.animateTo(targetValue = 1f, animationSpec = animationSpec)
            } finally {
                // Also reached when the board leaves the composition mid-animation (for
                // example when the Activity is recreated), so the move is not lost. It is
                // only dropped when the board itself was replaced (restart, load).
                if (currentBoard === boardAtStart) {
                    currentOnPieceMove(move.from, move.to, move.promotion)
                }
            }
            // Unlocks the board if the ViewModel did not accept the move.
            userMove = null
        }
    }

    val startMove: (ChessPiece, Position) -> Unit = { piece, targetPosition ->
        selectedPosition = null
        if (piece.isPromotingMove(targetPosition)) {
            pendingPromotion = piece to targetPosition
        } else {
            userMove = AnimatedMove(
                from = piece.position,
                to = targetPosition,
                promotion = null,
                isAiMove = false
            )
        }
    }

    val onSquareClick: (Position) -> Unit = onSquareClick@{ position ->
        if (isLocked) return@onSquareClick
        val piece = selectedPiece
        if (piece != null && position in legalTargets) {
            startMove(piece, position)
            return@onSquareClick
        }
        selectedPosition = when {
            // Tapping the selected piece again deselects it.
            position == piece?.position -> null
            chessBoardState.getPieceAtPosition(position)?.color == chessBoardState.playerTurn -> position
            else -> null
        }
    }

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val boardSize = min(maxWidth, maxHeight)
        val squareSize = boardSize / BOARD_SIZE

        Box(modifier = Modifier.size(boardSize)) {
            // Squares: colors, move hints and all input.
            Column(modifier = Modifier.fillMaxSize()) {
                for (row in 0 until BOARD_SIZE) {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        for (column in 0 until BOARD_SIZE) {
                            val position = Position(row, column)
                            BoardSquare(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                position = position,
                                piece = chessBoardState.getPieceAtPosition(position),
                                isSelected = position == selectedPiece?.position,
                                isTarget = position in legalTargets,
                                isEnabled = !isLocked,
                                onClick = { onSquareClick(position) }
                            )
                        }
                    }
                }
            }

            // Pieces: one layer, positioned from the current square size, so a resize never
            // leaves them at stale offsets. They draw only; the squares below take the taps.
            chessBoardState.cells.forEachIndexed { row, rowPieces ->
                rowPieces.forEachIndexed { column, piece ->
                    if (piece != null) {
                        val position = Position(row, column)
                        key(position) {
                            val target = motion?.slides?.get(position)
                            val isCaptured = position == motion?.captured
                            PieceView(
                                modifier = Modifier
                                    // Moving pieces are drawn above all others, so a
                                    // capture never slides beneath the captured piece.
                                    .zIndex(if (target != null) MOVING_PIECE_Z_INDEX else 0f)
                                    .offset {
                                        val squarePx = squareSize.toPx()
                                        val fraction = if (target != null) moveProgress.value else 0f
                                        val rowDelta = target?.let { it.row - row } ?: 0
                                        val columnDelta = target?.let { it.column - column } ?: 0
                                        IntOffset(
                                            x = ((column + columnDelta * fraction) * squarePx).roundToInt(),
                                            y = ((row + rowDelta * fraction) * squarePx).roundToInt()
                                        )
                                    }
                                    // The captured piece fades out as the mover arrives.
                                    .graphicsLayer {
                                        alpha = if (isCaptured) 1f - moveProgress.value else 1f
                                    }
                                    .size(squareSize),
                                piece = piece,
                                // The square describes its piece.
                                contentDescription = null
                            )
                        }
                    }
                }
            }

            pendingPromotion?.let { (piece, targetPosition) ->
                PromotionPicker(
                    modifier = Modifier
                        .matchParentSize()
                        .zIndex(PROMOTION_PICKER_Z_INDEX),
                    color = piece.color,
                    onSelect = { promotion ->
                        pendingPromotion = null
                        userMove = AnimatedMove(
                            from = piece.position,
                            to = targetPosition,
                            promotion = promotion,
                            isAiMove = false
                        )
                    },
                    onDismiss = { pendingPromotion = null }
                )
            }
        }
    }
}

/**
 * One square of the board: its color, a move hint and the input for that square. It is a
 * single accessible button named after the square and the piece on it.
 */
@Composable
private fun BoardSquare(
    modifier: Modifier,
    position: Position,
    piece: ChessPiece?,
    isSelected: Boolean,
    isTarget: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalBoardColors.current
    val isLightSquare = (position.row + position.column) % 2 == 0
    val isCapture = isTarget && piece != null
    val description = squareDescription(position, piece, isSelected, isTarget)
    val clickLabel = when {
        isCapture -> Strings.CAPTURE
        isTarget -> Strings.MOVE_HERE
        piece != null -> Strings.SELECT_PIECE
        else -> null
    }
    val highlight = if (isSelected || isCapture) {
        Modifier.background(colors.highlight)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .background(if (isLightSquare) colors.lightSquare else colors.darkSquare)
            .then(highlight)
            .semantics {
                contentDescription = description
                selected = isSelected
            }
            .clickable(
                enabled = isEnabled,
                onClickLabel = clickLabel,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isTarget && piece == null) {
            DrawAvailableMoves()
        }
    }
}

/** For example "e4, White knight, can be captured". */
private fun squareDescription(
    position: Position,
    piece: ChessPiece?,
    isSelected: Boolean,
    isTarget: Boolean
): String = buildList {
    add(position.toAlgebraic())
    add(piece?.let { Strings.pieceName(it.color, it.type) } ?: Strings.EMPTY_SQUARE)
    if (isSelected) add(Strings.SELECTED)
    if (isTarget) add(if (piece != null) Strings.POSSIBLE_CAPTURE else Strings.POSSIBLE_MOVE)
}.joinToString(separator = ", ")

/**
 * Works out how [move] looks on this board: castling also slides the rook to the square the
 * king passes over, and en passant fades out the pawn beside the capturing pawn.
 */
private fun ChessBoard.motionOf(move: AnimatedMove): MoveMotion? {
    val piece = getPieceAtPosition(move.from) ?: return null
    val slides = mutableMapOf(move.from to move.to)
    val targetPiece = getPieceAtPosition(move.to)
    var captured = move.to.takeIf { targetPiece != null && targetPiece.color != piece.color }
    val columnDelta = move.to.column - move.from.column

    when (piece.type) {
        PieceType.King -> if (abs(columnDelta) == CASTLING_KING_STEP) {
            val kingSide = columnDelta > 0
            val rookColumn = if (kingSide) {
                CastlingRights.KING_SIDE_ROOK_COLUMN
            } else {
                CastlingRights.QUEEN_SIDE_ROOK_COLUMN
            }
            val rookFrom = Position(move.from.row, rookColumn)
            val rookTo = Position(move.from.row, move.from.column + if (kingSide) 1 else -1)
            if (getPieceAtPosition(rookFrom)?.type == PieceType.Rook) {
                slides[rookFrom] = rookTo
            }
        }
        PieceType.Pawn -> if (columnDelta != 0 && targetPiece == null) {
            captured = Position(move.from.row, move.to.column)
        }
        else -> Unit
    }
    return MoveMotion(slides = slides, captured = captured)
}

/** True if this is a pawn and [targetPosition] is on the last rank for its color. */
private fun ChessPiece.isPromotingMove(targetPosition: Position): Boolean {
    val lastRow = if (color == PieceColor.White) 0 else BOARD_SIZE - 1
    return type == PieceType.Pawn && targetPosition.row == lastRow
}

private const val MOVING_PIECE_Z_INDEX = 1f
private const val CASTLING_KING_STEP = 2
private const val PROMOTION_PICKER_Z_INDEX = 2f
