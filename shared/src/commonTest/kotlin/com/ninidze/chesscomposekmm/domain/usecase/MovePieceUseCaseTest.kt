package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.pieces.Pawn
import com.ninidze.chesscomposekmm.domain.result.Resource
import com.ninidze.chesscomposekmm.testutil.play
import com.ninidze.chesscomposekmm.testutil.playAll
import com.ninidze.chesscomposekmm.testutil.sq
import com.ninidze.chesscomposekmm.util.Constants.INVALID_MOVE_MESSAGE
import com.ninidze.chesscomposekmm.util.Constants.NOT_PLAYERS_TURN
import com.ninidze.chesscomposekmm.util.Constants.PIECE_NOT_FOUND
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MovePieceUseCaseTest {

    private val movePiece = MovePieceUseCase(CheckGameEndUseCase())
    private val start = ChessBoard.createInitialChessBoard()

    @Test
    fun movingTheOpponentsPieceIsRejected() {
        val blackPawn = start.getPieceAtPosition(sq("e7"))!!
        assertEquals(Resource.Failure(NOT_PLAYERS_TURN), movePiece(start, blackPawn, sq("e5")))
    }

    @Test
    fun illegalTargetIsRejected() {
        val pawn = start.getPieceAtPosition(sq("e2"))!!
        assertEquals(Resource.Failure(INVALID_MOVE_MESSAGE), movePiece(start, pawn, sq("e5")))
    }

    @Test
    fun pieceThatIsNotOnTheBoardIsRejected() {
        val ghost = Pawn(PieceColor.White, sq("e4"))
        assertEquals(Resource.Failure(PIECE_NOT_FOUND), movePiece(start, ghost, sq("e5")))
    }

    @Test
    fun pieceFromAnOlderBoardIsResolvedBySquare() {
        // The piece instance comes from the start board, the move is applied to a later one.
        val later = start.playAll("g1f3", "g8f6")
        val stalePawn = start.getPieceAtPosition(sq("e2"))!!

        assertIs<Resource.Success<*>>(movePiece(later, stalePawn, sq("e4")))
    }

    @Test
    fun turnAndCountersAdvance() {
        val afterWhite = start.play("g1", "f3")
        assertEquals(PieceColor.Black, afterWhite.playerTurn)
        assertEquals(1, afterWhite.halfmoveClock)
        assertEquals(1, afterWhite.fullmoveNumber)

        val afterBlack = afterWhite.play("g8", "f6")
        assertEquals(PieceColor.White, afterBlack.playerTurn)
        assertEquals(2, afterBlack.halfmoveClock)
        assertEquals(2, afterBlack.fullmoveNumber)
    }

    @Test
    fun irreversibleMoveRestartsThePositionHistory() {
        val board = start.playAll("g1f3", "g8f6", "e2e4")
        assertEquals(1, board.positionHistory.size)
    }
}
