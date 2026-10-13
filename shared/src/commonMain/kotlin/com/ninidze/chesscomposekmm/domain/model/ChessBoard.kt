package com.ninidze.chesscomposekmm.domain.model

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.PieceColor.Black
import com.ninidze.chesscomposekmm.domain.model.PieceColor.White
import com.ninidze.chesscomposekmm.domain.pieces.Bishop
import com.ninidze.chesscomposekmm.domain.pieces.King
import com.ninidze.chesscomposekmm.domain.pieces.Knight
import com.ninidze.chesscomposekmm.domain.pieces.Pawn
import com.ninidze.chesscomposekmm.domain.pieces.Queen
import com.ninidze.chesscomposekmm.domain.pieces.Rook
import com.ninidze.chesscomposekmm.util.Constants.BOARD_SIZE
import com.ninidze.chesscomposekmm.util.extensions.positionKey

/**
 * @property castlingRights The castling moves each side may still make.
 * @property enPassantTarget The square a pawn just skipped with a two-square advance, which
 * an opposing pawn may capture onto this move; null otherwise.
 * @property halfmoveClock Half-moves since the last capture or pawn move (50-move rule).
 * @property fullmoveNumber Starts at 1 and is incremented after every Black move.
 * @property positionHistory Repetition keys ([positionKey]) of the positions reached since
 * the last capture or pawn move, the current position last. Older positions can never recur.
 */
data class ChessBoard(
    val cells: List<List<ChessPiece?>>,
    val playerTurn: PieceColor,
    val result: GameResult = GameResult.Ongoing,
    val castlingRights: CastlingRights = CastlingRights.None,
    val enPassantTarget: Position? = null,
    val halfmoveClock: Int = 0,
    val fullmoveNumber: Int = 1,
    val positionHistory: List<String> = emptyList()
) {
    /** The side that delivered checkmate, or null if the game is not won. */
    val winner: PieceColor?
        get() = (result as? GameResult.Checkmate)?.winner

    val isGameOver: Boolean
        get() = result.isGameOver

    fun getPieceAtPosition(position: Position): ChessPiece? = cells[position.row][position.column]

    companion object {

        /**
         * Creates an initial chess board with all pieces in their starting positions.
         *
         * @return A ChessBoard object representing the initial state of a game of chess.
         */
        fun createInitialChessBoard(): ChessBoard {
            val cells = List(BOARD_SIZE) { row ->
                List(BOARD_SIZE) { column ->
                    initialPieceAt(row, column)
                }
            }
            val board = ChessBoard(
                cells = cells,
                playerTurn = White,
                castlingRights = CastlingRights.All
            )
            return board.copy(positionHistory = listOf(board.positionKey()))
        }

        /**
         * Returns the chess piece that should be placed at a given position on the chess board at the start of a game.
         *
         * @param row The row of the position.
         * @param column The column of the position.
         * @return A ChessPiece object representing the piece that should be placed at the given position,
         * or null if the position should be empty.
         */
        private fun initialPieceAt(row: Int, column: Int): ChessPiece? {
            val color = if (row < BOARD_SIZE / 2) Black else White
            return when (row) {
                in arrayOf(0, 7) -> createMajorPiece(color, row, column)
                in arrayOf(1, 6) -> Pawn(color, Position(row, column))
                else -> null
            }
        }

        /**
         * Creates a major chess piece (rook, knight, bishop, queen, or king) at a given position.
         *
         * @param color The color of the piece to be created.
         * @param row The row of the position.
         * @param column The column of the position.
         * @return A ChessPiece object representing the major piece that should be placed at the given position,
         * or null if the position should be empty.
         */
        private fun createMajorPiece(color: PieceColor, row: Int, column: Int): ChessPiece? {
            val position = Position(row, column)
            return when (column) {
                0, 7 -> Rook(color, position)
                1, 6 -> Knight(color, position)
                2, 5 -> Bishop(color, position)
                3 -> Queen(color, position)
                4 -> King(color, position)
                else -> null
            }
        }
    }
}
