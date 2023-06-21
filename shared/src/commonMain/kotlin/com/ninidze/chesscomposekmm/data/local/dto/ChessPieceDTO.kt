package com.ninidze.chesscomposekmm.data.local.dto

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.pieces.Bishop
import com.ninidze.chesscomposekmm.domain.pieces.King
import com.ninidze.chesscomposekmm.domain.pieces.Knight
import com.ninidze.chesscomposekmm.domain.pieces.Pawn
import com.ninidze.chesscomposekmm.domain.pieces.Queen
import com.ninidze.chesscomposekmm.domain.pieces.Rook
import kotlinx.serialization.Serializable

@Serializable
data class ChessPieceDTO(
    val color: PieceColor,
    val position: PositionDTO,
    val type: PieceType
) {
    fun toDomainModel(): ChessPiece {
        return when (type) {
            PieceType.Pawn -> Pawn(color, position.toDomainModel())
            PieceType.King -> King(color, position.toDomainModel())
            PieceType.Queen -> Queen(color, position.toDomainModel())
            PieceType.Rook -> Rook(color, position.toDomainModel())
            PieceType.Bishop -> Bishop(color, position.toDomainModel())
            PieceType.Knight -> Knight(color, position.toDomainModel())
        }
    }

    companion object {
        fun fromDomainModel(domainModel: ChessPiece) = ChessPieceDTO(
            color = domainModel.color,
            position = PositionDTO.fromDomainModel(domainModel.position),
            type = domainModel.type
        )
    }
}
