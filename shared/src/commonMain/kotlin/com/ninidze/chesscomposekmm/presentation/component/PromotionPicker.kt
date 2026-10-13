package com.ninidze.chesscomposekmm.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.pieces.Bishop
import com.ninidze.chesscomposekmm.domain.pieces.Knight
import com.ninidze.chesscomposekmm.domain.pieces.Queen
import com.ninidze.chesscomposekmm.domain.pieces.Rook
import com.ninidze.chesscomposekmm.presentation.theme.BoardWhite
import com.ninidze.chesscomposekmm.util.extensions.noRippleClickable

/**
 * An overlay that asks which piece a pawn of [color] promotes to.
 * Tapping outside the choices calls [onDismiss], which cancels the move.
 */
@Composable
fun PromotionPicker(
    modifier: Modifier = Modifier,
    color: PieceColor,
    onSelect: (PieceType) -> Unit,
    onDismiss: () -> Unit
) {
    // Only used to draw the choices, so the position is irrelevant.
    val placeholder = Position(0, 0)
    val choices = listOf(
        Queen(color, placeholder),
        Rook(color, placeholder),
        Bishop(color, placeholder),
        Knight(color, placeholder)
    )
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .noRippleClickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(PICKER_WIDTH_FRACTION)
                .background(BoardWhite, RoundedCornerShape(12.dp))
                // Taps between the choices must not dismiss the picker.
                .noRippleClickable { }
                .padding(8.dp)
        ) {
            choices.forEach { choice ->
                PieceView(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    piece = choice,
                    onPieceClick = { onSelect(choice.type) }
                )
            }
        }
    }
}

private const val SCRIM_ALPHA = 0.5f
private const val PICKER_WIDTH_FRACTION = 0.8f
