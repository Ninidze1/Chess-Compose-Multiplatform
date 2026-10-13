package com.ninidze.chesscomposekmm.domain.rules

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.testutil.boardOf
import com.ninidze.chesscomposekmm.testutil.movesFrom
import com.ninidze.chesscomposekmm.testutil.play
import com.ninidze.chesscomposekmm.testutil.playAll
import com.ninidze.chesscomposekmm.testutil.sq
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EnPassantTest {

    @Test
    fun doublePushSetsTheTargetSquare() {
        val board = ChessBoard.createInitialChessBoard().play("e2", "e4")
        assertEquals(sq("e3"), board.enPassantTarget)
    }

    @Test
    fun singlePushClearsTheTargetSquare() {
        val board = ChessBoard.createInitialChessBoard().playAll("e2e4", "e7e6")
        assertNull(board.enPassantTarget)
    }

    @Test
    fun capturedPawnIsRemoved() {
        val afterPush = boardOf("4k3/8/8/8/3p4/8/4P3/4K3").play("e2", "e4")
        assertTrue(sq("e3") in afterPush.movesFrom("d4"))

        val board = afterPush.play("d4", "e3")

        assertEquals(PieceType.Pawn, board.getPieceAtPosition(sq("e3"))?.type)
        assertEquals(PieceColor.Black, board.getPieceAtPosition(sq("e3"))?.color)
        assertNull(board.getPieceAtPosition(sq("e4")))
        assertNull(board.getPieceAtPosition(sq("d4")))
        assertNull(board.enPassantTarget)
        assertEquals(0, board.halfmoveClock)
    }

    @Test
    fun whiteCapturesEnPassant() {
        val board = boardOf("4k3/3p4/8/4P3/8/8/8/4K3", playerTurn = PieceColor.Black)
            .play("d7", "d5")
            .play("e5", "d6")

        assertEquals(PieceType.Pawn, board.getPieceAtPosition(sq("d6"))?.type)
        assertNull(board.getPieceAtPosition(sq("d5")))
    }

    @Test
    fun onlyAvailableImmediatelyAfterTheDoublePush() {
        val board = boardOf("4k3/8/8/8/3p4/8/4P3/4K3").playAll("e2e4", "e8d8", "e1d1")
        assertFalse(sq("e3") in board.movesFrom("d4"))
    }

    @Test
    fun notAllowedWhenItExposesTheKingAlongTheRank() {
        // Capturing on d6 would remove both pawns from the fifth rank and expose the a5 king
        // to the h5 rook.
        val board = boardOf("4k3/3p4/8/K3P2r/8/8/8/8", playerTurn = PieceColor.Black)
            .play("d7", "d5")

        assertEquals(sq("d6"), board.enPassantTarget)
        assertFalse(sq("d6") in board.movesFrom("e5"))
    }
}
