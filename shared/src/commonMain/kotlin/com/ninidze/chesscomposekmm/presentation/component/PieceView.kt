package com.ninidze.chesscomposekmm.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.PieceColor.White
import com.ninidze.chesscomposekmm.domain.model.PieceType.Bishop
import com.ninidze.chesscomposekmm.domain.model.PieceType.King
import com.ninidze.chesscomposekmm.domain.model.PieceType.Knight
import com.ninidze.chesscomposekmm.domain.model.PieceType.Pawn
import com.ninidze.chesscomposekmm.domain.model.PieceType.Queen
import com.ninidze.chesscomposekmm.domain.model.PieceType.Rook
import com.ninidze.chesscomposekmm.presentation.Strings
import com.ninidze.chesscomposekmm.util.extensions.noRippleClickable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

/**
 * Draws [piece]. With [onPieceClick] it is a button; without it, it only draws and lets
 * taps reach whatever lies below (the board squares handle input themselves).
 *
 * @param contentDescription Read by accessibility services; null when an enclosing element
 * already describes the piece.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun PieceView(
    modifier: Modifier = Modifier,
    piece: ChessPiece,
    contentDescription: String? = Strings.pieceName(piece.color, piece.type),
    onPieceClick: (() -> Unit)? = null
) {
    val imageResId = when (piece.type) {
        King -> if (piece.color == White) "drawables/ic_king_white.xml" else "drawables/ic_king_black.xml"
        Queen -> if (piece.color == White) "drawables/ic_queen_white.xml" else "drawables/ic_queen_black.xml"
        Rook -> if (piece.color == White) "drawables/ic_rook_white.xml" else "drawables/ic_rook_black.xml"
        Bishop -> if (piece.color == White) "drawables/ic_bishop_white.xml" else "drawables/ic_bishop_black.xml"
        Knight -> if (piece.color == White) "drawables/ic_knight_white.xml" else "drawables/ic_knight_black.xml"
        Pawn -> if (piece.color == White) "drawables/ic_pawn_white.xml" else "drawables/ic_pawn_black.xml"
    }

    val clickModifier = if (onPieceClick != null) {
        Modifier.noRippleClickable(role = Role.Button) { onPieceClick() }
    } else {
        Modifier
    }

    Image(
        painter = painterResource(res = imageResId),
        contentDescription = contentDescription,
        modifier = modifier
            .then(clickModifier)
            .padding(6.dp)
    )
}
