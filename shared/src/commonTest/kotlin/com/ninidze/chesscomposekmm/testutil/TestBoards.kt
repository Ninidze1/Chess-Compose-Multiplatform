package com.ninidze.chesscomposekmm.testutil

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.CastlingRights
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.GameResult
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.pieces.Bishop
import com.ninidze.chesscomposekmm.domain.pieces.King
import com.ninidze.chesscomposekmm.domain.pieces.Knight
import com.ninidze.chesscomposekmm.domain.pieces.Pawn
import com.ninidze.chesscomposekmm.domain.pieces.Queen
import com.ninidze.chesscomposekmm.domain.pieces.Rook
import com.ninidze.chesscomposekmm.domain.result.Resource
import com.ninidze.chesscomposekmm.domain.usecase.CheckGameEndUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import com.ninidze.chesscomposekmm.util.Constants.BOARD_SIZE
import com.ninidze.chesscomposekmm.util.FenConverter
import com.ninidze.chesscomposekmm.util.extensions.positionKey

const val START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

val testMovePieceUseCase = MovePieceUseCase(CheckGameEndUseCase())

/** The square named in algebraic notation, such as "e4". Row 0 is rank 8. */
fun sq(name: String): Position {
    require(name.length == 2) { "Not a square: $name" }
    val column = name[0] - 'a'
    val rank = name[1] - '0'
    val position = Position(row = BOARD_SIZE - rank, column = column)
    require(position.isValidPosition()) { "Not a square: $name" }
    return position
}

/** The piece for a FEN letter: upper case is White, lower case is Black. */
fun pieceOf(letter: Char, position: Position): ChessPiece {
    val color = if (letter.isUpperCase()) PieceColor.White else PieceColor.Black
    return when (letter.lowercaseChar()) {
        'p' -> Pawn(color, position)
        'n' -> Knight(color, position)
        'b' -> Bishop(color, position)
        'r' -> Rook(color, position)
        'q' -> Queen(color, position)
        'k' -> King(color, position)
        else -> error("Unknown piece letter: $letter")
    }
}

/**
 * Builds a board from the piece placement field of a FEN, such as
 * "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR". The position history starts with this
 * position, as it does for a new game.
 */
fun boardOf(
    placement: String,
    playerTurn: PieceColor = PieceColor.White,
    castlingRights: CastlingRights = CastlingRights.None,
    enPassantTarget: Position? = null,
    halfmoveClock: Int = 0,
    fullmoveNumber: Int = 1
): ChessBoard {
    val rows = placement.split('/')
    require(rows.size == BOARD_SIZE) { "Expected $BOARD_SIZE ranks: $placement" }
    val cells = rows.mapIndexed { row, rank ->
        val line = mutableListOf<ChessPiece?>()
        rank.forEach { symbol ->
            if (symbol.isDigit()) {
                repeat(symbol - '0') { line.add(null) }
            } else {
                line.add(pieceOf(symbol, Position(row, line.size)))
            }
        }
        require(line.size == BOARD_SIZE) { "Rank $rank does not have $BOARD_SIZE squares" }
        line.toList()
    }
    val board = ChessBoard(
        cells = cells,
        playerTurn = playerTurn,
        castlingRights = castlingRights,
        enPassantTarget = enPassantTarget,
        halfmoveClock = halfmoveClock,
        fullmoveNumber = fullmoveNumber
    )
    return board.copy(positionHistory = listOf(board.positionKey()))
}

/** Plays [from] -> [to] through [MovePieceUseCase] and fails the test if it is rejected. */
fun ChessBoard.play(from: String, to: String, promotion: PieceType = PieceType.Queen): ChessBoard =
    play(sq(from), sq(to), promotion)

fun ChessBoard.play(from: Position, to: Position, promotion: PieceType = PieceType.Queen): ChessBoard {
    val piece = getPieceAtPosition(from) ?: error("No piece on $from")
    return when (val result = testMovePieceUseCase(this, piece, to, promotion)) {
        is Resource.Success -> result.data
        is Resource.Failure -> error("Move $from -> $to rejected: ${result.message}")
    }
}

/** Plays moves written as "e2e4" one after another. */
fun ChessBoard.playAll(vararg moves: String): ChessBoard =
    moves.fold(this) { board, move ->
        require(move.length == 4) { "Expected a move like e2e4: $move" }
        board.play(move.substring(0, 2), move.substring(2, 4))
    }

/** Every legal move of the side to move, as from -> to pairs. */
fun ChessBoard.legalMoves(): List<Pair<Position, Position>> =
    cells.flatten()
        .filterNotNull()
        .filter { it.color == playerTurn }
        .flatMap { piece -> piece.getAvailableMoves(this).map { piece.position to it } }

/** The legal targets of the piece on [square]. */
fun ChessBoard.movesFrom(square: String): List<Position> {
    val piece = getPieceAtPosition(sq(square)) ?: error("No piece on $square")
    return piece.getAvailableMoves(this)
}

fun ChessBoard.toFen(): String = FenConverter().convertToFen(this)

/** The board with any game result cleared, so a test can keep playing from it. */
fun ChessBoard.ongoing(): ChessBoard = copy(result = GameResult.Ongoing)
