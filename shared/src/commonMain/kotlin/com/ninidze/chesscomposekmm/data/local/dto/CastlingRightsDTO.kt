package com.ninidze.chesscomposekmm.data.local.dto

import com.ninidze.chesscomposekmm.domain.model.CastlingRights
import kotlinx.serialization.Serializable

@Serializable
data class CastlingRightsDTO(
    val whiteKingSide: Boolean = false,
    val whiteQueenSide: Boolean = false,
    val blackKingSide: Boolean = false,
    val blackQueenSide: Boolean = false
) {
    fun toDomainModel() = CastlingRights(
        whiteKingSide = whiteKingSide,
        whiteQueenSide = whiteQueenSide,
        blackKingSide = blackKingSide,
        blackQueenSide = blackQueenSide
    )

    companion object {
        fun fromDomainModel(domainModel: CastlingRights) = CastlingRightsDTO(
            whiteKingSide = domainModel.whiteKingSide,
            whiteQueenSide = domainModel.whiteQueenSide,
            blackKingSide = domainModel.blackKingSide,
            blackQueenSide = domainModel.blackQueenSide
        )
    }
}
