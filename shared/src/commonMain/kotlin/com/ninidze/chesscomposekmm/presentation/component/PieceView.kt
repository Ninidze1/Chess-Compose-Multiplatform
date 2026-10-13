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
import com.ninidze.chesscomposekmm.resources.Res
import com.ninidze.chesscomposekmm.resources.ic_bishop_black
import com.ninidze.chesscomposekmm.resources.ic_bishop_white
import com.ninidze.chesscomposekmm.resources.ic_king_black
import com.ninidze.chesscomposekmm.resources.ic_king_white
import com.ninidze.chesscomposekmm.resources.ic_knight_black
import com.ninidze.chesscomposekmm.resources.ic_knight_white
import com.ninidze.chesscomposekmm.resources.ic_pawn_black
import com.ninidze.chesscomposekmm.resources.ic_pawn_white
import com.ninidze.chesscomposekmm.resources.ic_queen_black
import com.ninidze.chesscomposekmm.resources.ic_queen_white
import com.ninidze.chesscomposekmm.resources.ic_rook_black
import com.ninidze.chesscomposekmm.resources.ic_rook_white
import com.ninidze.chesscomposekmm.util.extensions.noRippleClickable
import org.jetbrains.compose.resources.painterResource

/**
 * Draws [piece]. With [onPieceClick] it is a button; without it, it only draws and lets
 * taps reach whatever lies below (the board squares handle input themselves).
 *
 * @param contentDescription Read by accessibility services; null when an enclosing element
 * already describes the piece.
 */
@Composable
fun PieceView(
    modifier: Modifier = Modifier,
    piece: ChessPiece,
    contentDescription: String? = Strings.pieceName(piece.color, piece.type),
    onPieceClick: (() -> Unit)? = null
) {
    val image = when (piece.type) {
        King -> if (piece.color == White) Res.drawable.ic_king_white else Res.drawable.ic_king_black
        Queen -> if (piece.color == White) Res.drawable.ic_queen_white else Res.drawable.ic_queen_black
        Rook -> if (piece.color == White) Res.drawable.ic_rook_white else Res.drawable.ic_rook_black
        Bishop -> if (piece.color == White) Res.drawable.ic_bishop_white else Res.drawable.ic_bishop_black
        Knight -> if (piece.color == White) Res.drawable.ic_knight_white else Res.drawable.ic_knight_black
        Pawn -> if (piece.color == White) Res.drawable.ic_pawn_white else Res.drawable.ic_pawn_black
    }

    val clickModifier = if (onPieceClick != null) {
        Modifier.noRippleClickable(role = Role.Button) { onPieceClick() }
    } else {
        Modifier
    }

    Image(
        painter = painterResource(image),
        contentDescription = contentDescription,
        modifier = modifier
            .then(clickModifier)
            .padding(6.dp)
    )
}
