package com.ninidze.chesscomposekmm.util

import com.ninidze.chesscomposekmm.domain.model.CastlingRights
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.testutil.START_FEN
import com.ninidze.chesscomposekmm.testutil.boardOf
import com.ninidze.chesscomposekmm.testutil.playAll
import com.ninidze.chesscomposekmm.testutil.sq
import kotlin.test.Test
import kotlin.test.assertEquals

class FenConverterTest {

    private val fenConverter = FenConverter()

    @Test
    fun startPosition() {
        assertEquals(START_FEN, fenConverter.convertToFen(ChessBoard.createInitialChessBoard()))
    }

    @Test
    fun doublePushWritesTheEnPassantSquare() {
        val board = ChessBoard.createInitialChessBoard().playAll("e2e4")
        assertEquals(
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
            fenConverter.convertToFen(board)
        )
    }

    @Test
    fun countersAndSideToMoveFollowTheGame() {
        val board = ChessBoard.createInitialChessBoard().playAll("e2e4", "c7c5", "g1f3")
        assertEquals(
            "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2",
            fenConverter.convertToFen(board)
        )
    }

    @Test
    fun lostCastlingRightsAreDropped() {
        val board = ChessBoard.createInitialChessBoard().playAll("e2e4", "e7e5", "e1e2")
        assertEquals(
            "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPPKPPP/RNBQ1BNR b kq - 1 2",
            fenConverter.convertToFen(board)
        )
    }

    @Test
    fun rightsWithoutKingOrRookOnTheHomeSquareAreDropped() {
        // The rights claim everything, but only the white king side still has its pieces home.
        val board = boardOf("4k3/8/8/8/8/8/8/4K2R", castlingRights = CastlingRights.All)
        assertEquals("4k3/8/8/8/8/8/8/4K2R w K - 0 1", fenConverter.convertToFen(board))
    }

    @Test
    fun noRightsIsADash() {
        val board = boardOf("4k3/8/8/8/8/8/8/4K3", playerTurn = PieceColor.Black)
        assertEquals("4k3/8/8/8/8/8/8/4K3 b - - 0 1", fenConverter.convertToFen(board))
    }

    @Test
    fun invalidCountersAreClamped() {
        val board = boardOf("4k3/8/8/8/8/8/8/4K3", halfmoveClock = -3, fullmoveNumber = 0)
        assertEquals("4k3/8/8/8/8/8/8/4K3 w - - 0 1", fenConverter.convertToFen(board))
    }

    @Test
    fun enPassantSquareIsWrittenAsGiven() {
        val board = boardOf("4k3/8/8/3pP3/8/8/8/4K3", enPassantTarget = sq("d6"))
        assertEquals("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1", fenConverter.convertToFen(board))
    }
}
