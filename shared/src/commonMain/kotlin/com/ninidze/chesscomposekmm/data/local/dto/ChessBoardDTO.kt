package com.ninidze.chesscomposekmm.data.local.dto

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.CastlingRights
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.GameResult
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.util.extensions.opposite
import com.ninidze.chesscomposekmm.util.extensions.positionKey
import kotlinx.serialization.Serializable

/**
 * Every field added after the first release has a default, so JSON saved by older builds
 * still decodes.
 *
 * @property winner Legacy field, kept so JSON saved by older builds still decodes.
 * New builds only write [result] and leave this null.
 * @property result The persisted game result. Null in JSON saved by older builds.
 * @property castlingRights Null in JSON saved by older builds; then inferred from the
 * placement of the kings and rooks.
 * @property positionHistory Empty in JSON saved by older builds; then the history starts
 * with the loaded position.
 */
@Serializable
data class ChessBoardDTO(
    val cells: List<List<ChessPieceDTO?>>,
    val playerTurn: PieceColor,
    val winner: PieceColor? = null,
    val result: GameResultDTO? = null,
    val castlingRights: CastlingRightsDTO? = null,
    val enPassantTarget: PositionDTO? = null,
    val halfmoveClock: Int = 0,
    val fullmoveNumber: Int = 1,
    val positionHistory: List<String> = emptyList()
) {
    fun toDomainModel(): ChessBoard {
        val cells = this.cells.map { row ->
            row.map { cell ->
                cell?.toDomainModel()
            }
        }
        val board = ChessBoard(
            cells = cells,
            playerTurn = this.playerTurn,
            result = this.result?.toDomainModel() ?: legacyResult(),
            castlingRights = this.castlingRights?.toDomainModel() ?: inferCastlingRights(cells),
            enPassantTarget = this.enPassantTarget?.toDomainModel()?.takeIf { isPlausibleEnPassant(it) },
            halfmoveClock = this.halfmoveClock.coerceAtLeast(0),
            fullmoveNumber = this.fullmoveNumber.coerceAtLeast(1)
        )
        return board.copy(positionHistory = positionHistory.ifEmpty { listOf(board.positionKey()) })
    }

    /**
     * Older builds had no [result] and stored the checkmated side in [winner],
     * so the actual winner is the opposite color.
     */
    private fun legacyResult(): GameResult =
        winner?.let { GameResult.Checkmate(winner = it.opposite()) } ?: GameResult.Ongoing

    /** The target lies on the square behind a pawn of the side that just moved. */
    private fun isPlausibleEnPassant(target: Position): Boolean {
        val expectedRow = if (playerTurn == PieceColor.White) {
            WHITE_TO_MOVE_EN_PASSANT_ROW
        } else {
            BLACK_TO_MOVE_EN_PASSANT_ROW
        }
        return target.isValidPosition() && target.row == expectedRow
    }

    /**
     * Older builds did not track castling rights. Grant a right wherever the king and the
     * rook still stand on their home squares; that is the best guess without the move list.
     */
    private fun inferCastlingRights(cells: List<List<ChessPiece?>>): CastlingRights {
        fun hasPiece(color: PieceColor, type: PieceType, column: Int): Boolean {
            val piece = cells.getOrNull(CastlingRights.homeRow(color))?.getOrNull(column)
            return piece != null && piece.color == color && piece.type == type
        }
        fun inferred(color: PieceColor, rookColumn: Int): Boolean =
            hasPiece(color, PieceType.King, CastlingRights.KING_COLUMN) &&
                hasPiece(color, PieceType.Rook, rookColumn)
        return CastlingRights(
            whiteKingSide = inferred(PieceColor.White, CastlingRights.KING_SIDE_ROOK_COLUMN),
            whiteQueenSide = inferred(PieceColor.White, CastlingRights.QUEEN_SIDE_ROOK_COLUMN),
            blackKingSide = inferred(PieceColor.Black, CastlingRights.KING_SIDE_ROOK_COLUMN),
            blackQueenSide = inferred(PieceColor.Black, CastlingRights.QUEEN_SIDE_ROOK_COLUMN)
        )
    }

    companion object {
        // Black's two-square advance skips row 2 (rank 6), White's skips row 5 (rank 3).
        private const val WHITE_TO_MOVE_EN_PASSANT_ROW = 2
        private const val BLACK_TO_MOVE_EN_PASSANT_ROW = 5

        fun fromDomainModel(domainModel: ChessBoard) = ChessBoardDTO(
            cells = domainModel.cells.map { row ->
                row.map { cell ->
                    cell?.let { ChessPieceDTO.fromDomainModel(it) }
                }
            },
            playerTurn = domainModel.playerTurn,
            result = GameResultDTO.fromDomainModel(domainModel.result),
            castlingRights = CastlingRightsDTO.fromDomainModel(domainModel.castlingRights),
            enPassantTarget = domainModel.enPassantTarget?.let { PositionDTO.fromDomainModel(it) },
            halfmoveClock = domainModel.halfmoveClock,
            fullmoveNumber = domainModel.fullmoveNumber,
            positionHistory = domainModel.positionHistory
        )
    }
}
