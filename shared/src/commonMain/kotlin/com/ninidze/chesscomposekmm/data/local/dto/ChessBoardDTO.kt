package com.ninidze.chesscomposekmm.data.local.dto

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import kotlinx.serialization.Serializable

@Serializable
data class ChessBoardDTO(
    val cells: List<List<ChessPieceDTO?>>,
    val playerTurn: PieceColor,
    val winner : PieceColor? = null
) {
    fun toDomainModel() = ChessBoard(
        cells = this.cells.map { row ->
            row.map { cell ->
                cell?.toDomainModel()
            }
        },
        playerTurn = this.playerTurn,
        winner = this.winner
    )

    companion object {
        fun fromDomainModel(domainModel: ChessBoard) = ChessBoardDTO(
            cells = domainModel.cells.map { row ->
                row.map { cell ->
                    cell?.let { ChessPieceDTO.fromDomainModel(it) }
                }
            },
            playerTurn = domainModel.playerTurn,
            winner = domainModel.winner
        )
    }
}