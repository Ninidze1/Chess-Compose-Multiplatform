package com.ninidze.chesscomposekmm.data.local.dto

import com.ninidze.chesscomposekmm.domain.model.Position
import kotlinx.serialization.Serializable

@Serializable
data class PositionDTO(val row: Int, val column: Int) {
    fun toDomainModel() = Position(
        row = this.row,
        column = this.column
    )

    companion object {
        fun fromDomainModel(domainModel: Position) = PositionDTO(
            row = domainModel.row,
            column = domainModel.column
        )
    }
}
