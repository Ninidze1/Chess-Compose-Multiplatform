package com.ninidze.chesscomposekmm.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.PieceColor.White
import com.ninidze.chesscomposekmm.domain.model.PieceType.Bishop
import com.ninidze.chesscomposekmm.domain.model.PieceType.King
import com.ninidze.chesscomposekmm.domain.model.PieceType.Knight
import com.ninidze.chesscomposekmm.domain.model.PieceType.Pawn
import com.ninidze.chesscomposekmm.domain.model.PieceType.Queen
import com.ninidze.chesscomposekmm.domain.model.PieceType.Rook
import com.ninidze.chesscomposekmm.util.extensions.noRippleClickable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PieceView(
    piece: ChessPiece,
    onPieceClick: () -> Unit
) {
    val imageResId = when (piece.type) {
        King -> if (piece.color == White) "drawables/ic_king_white.xml" else "drawables/ic_king_black.xml"
        Queen -> if (piece.color == White) "drawables/ic_queen_white.xml" else "drawables/ic_queen_black.xml"
        Rook -> if (piece.color == White) "drawables/ic_rook_white.xml" else "drawables/ic_rook_black.xml"
        Bishop -> if (piece.color == White) "drawables/ic_bishop_white.xml" else "drawables/ic_bishop_black.xml"
        Knight -> if (piece.color == White) "drawables/ic_knight_white.xml" else "drawables/ic_knight_black.xml"
        Pawn -> if (piece.color == White) "drawables/ic_pawn_white.xml" else "drawables/ic_pawn_black.xml"
    }

    Image(
        painter = painterResource(res = imageResId),
        contentDescription = "${piece.type} ${piece.color}",
        modifier = Modifier
            .size(42.dp)
            .noRippleClickable { onPieceClick() }
    )
}