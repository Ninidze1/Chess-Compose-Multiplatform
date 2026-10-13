package com.ninidze.chesscomposekmm.data.local.dto

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.GameResult
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.util.extensions.opposite
import kotlinx.serialization.Serializable

/**
 * @property winner Legacy field, kept so JSON saved by older builds still decodes.
 * New builds only write [result] and leave this null.
 * @property result The persisted game result. Null in JSON saved by older builds.
 */
@Serializable
data class ChessBoardDTO(
    val cells: List<List<ChessPieceDTO?>>,
    val playerTurn: PieceColor,
    val winner : PieceColor? = null,
    val result: GameResultDTO? = null
) {
    fun toDomainModel() = ChessBoard(
        cells = this.cells.map { row ->
            row.map { cell ->
                cell?.toDomainModel()
            }
        },
        playerTurn = this.playerTurn,
        result = this.result?.toDomainModel() ?: legacyResult()
    )

    /**
     * Older builds had no [result] and stored the checkmated side in [winner],
     * so the actual winner is the opposite color.
     */
    private fun legacyResult(): GameResult =
        winner?.let { GameResult.Checkmate(winner = it.opposite()) } ?: GameResult.Ongoing

    companion object {
        fun fromDomainModel(domainModel: ChessBoard) = ChessBoardDTO(
            cells = domainModel.cells.map { row ->
                row.map { cell ->
                    cell?.let { ChessPieceDTO.fromDomainModel(it) }
                }
            },
            playerTurn = domainModel.playerTurn,
            result = GameResultDTO.fromDomainModel(domainModel.result)
        )
    }
}
