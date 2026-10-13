package com.ninidze.chesscomposekmm.data.local.dto

import com.ninidze.chesscomposekmm.domain.model.DrawReason
import com.ninidze.chesscomposekmm.domain.model.GameResult
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import kotlinx.serialization.Serializable

@Serializable
enum class GameResultType {
    Ongoing,
    Checkmate,
    Stalemate,
    Draw
}

@Serializable
data class GameResultDTO(val type: GameResultType, val winner: PieceColor? = null, val drawReason: DrawReason? = null) {
    /**
     * Falls back to [GameResult.Ongoing] when a required field is missing, so a malformed
     * entry never blocks loading the board.
     */
    fun toDomainModel(): GameResult = when (type) {
        GameResultType.Ongoing -> GameResult.Ongoing
        GameResultType.Checkmate -> winner?.let { GameResult.Checkmate(it) } ?: GameResult.Ongoing
        GameResultType.Stalemate -> GameResult.Stalemate
        GameResultType.Draw -> drawReason?.let { GameResult.Draw(it) } ?: GameResult.Ongoing
    }

    companion object {
        fun fromDomainModel(domainModel: GameResult) = when (domainModel) {
            is GameResult.Ongoing -> GameResultDTO(type = GameResultType.Ongoing)
            is GameResult.Checkmate -> GameResultDTO(
                type = GameResultType.Checkmate,
                winner = domainModel.winner
            )
            is GameResult.Stalemate -> GameResultDTO(type = GameResultType.Stalemate)
            is GameResult.Draw -> GameResultDTO(
                type = GameResultType.Draw,
                drawReason = domainModel.reason
            )
        }
    }
}
