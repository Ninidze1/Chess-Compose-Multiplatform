package com.ninidze.chesscomposekmm.domain.rules

import com.ninidze.chesscomposekmm.domain.model.CastlingRights
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

class CastlingTest {

    private val whiteRights = CastlingRights(whiteKingSide = true, whiteQueenSide = true)

    @Test
    fun bothWingsAllowedWhenPathIsClearAndSafe() {
        val board = boardOf("r3k2r/8/8/8/8/8/8/R3K2R", castlingRights = CastlingRights.All)

        val moves = board.movesFrom("e1")
        assertTrue(sq("g1") in moves)
        assertTrue(sq("c1") in moves)
    }

    @Test
    fun kingSideCastlingMovesTheRookAndClearsTheRights() {
        val board = boardOf("r3k2r/8/8/8/8/8/8/R3K2R", castlingRights = CastlingRights.All)
            .play("e1", "g1")

        assertEquals(PieceType.King, board.getPieceAtPosition(sq("g1"))?.type)
        assertEquals(PieceType.Rook, board.getPieceAtPosition(sq("f1"))?.type)
        assertNull(board.getPieceAtPosition(sq("h1")))
        assertNull(board.getPieceAtPosition(sq("e1")))
        assertEquals(
            CastlingRights(blackKingSide = true, blackQueenSide = true),
            board.castlingRights
        )
    }

    @Test
    fun queenSideCastlingMovesTheRook() {
        val board = boardOf("r3k2r/8/8/8/8/8/8/R3K2R", castlingRights = CastlingRights.All)
            .play("e1", "c1")

        assertEquals(PieceType.King, board.getPieceAtPosition(sq("c1"))?.type)
        assertEquals(PieceType.Rook, board.getPieceAtPosition(sq("d1"))?.type)
        assertNull(board.getPieceAtPosition(sq("a1")))
        assertNull(board.getPieceAtPosition(sq("b1")))
    }

    @Test
    fun blackCanCastleToo() {
        val board = boardOf(
            "r3k2r/8/8/8/8/8/8/R3K2R",
            playerTurn = PieceColor.Black,
            castlingRights = CastlingRights.All
        ).play("e8", "c8")

        assertEquals(PieceType.King, board.getPieceAtPosition(sq("c8"))?.type)
        assertEquals(PieceType.Rook, board.getPieceAtPosition(sq("d8"))?.type)
        assertFalse(board.castlingRights.blackKingSide)
        assertFalse(board.castlingRights.blackQueenSide)
        assertTrue(board.castlingRights.whiteKingSide)
    }

    @Test
    fun deniedWhileInCheck() {
        // The e8 rook checks the king.
        val moves = boardOf("k3r3/8/8/8/8/8/8/R3K2R", castlingRights = whiteRights).movesFrom("e1")
        assertFalse(sq("g1") in moves)
        assertFalse(sq("c1") in moves)
    }

    @Test
    fun deniedThroughAnAttackedSquare() {
        // The f8 rook attacks f1, which the king would pass over; the queen side is unaffected.
        val moves = boardOf("k4r2/8/8/8/8/8/8/R3K2R", castlingRights = whiteRights).movesFrom("e1")
        assertFalse(sq("g1") in moves)
        assertTrue(sq("c1") in moves)
    }

    @Test
    fun deniedIntoCheck() {
        // The g8 rook attacks g1, where the king would land.
        val moves = boardOf("k5r1/8/8/8/8/8/8/R3K2R", castlingRights = whiteRights).movesFrom("e1")
        assertFalse(sq("g1") in moves)
        assertTrue(sq("c1") in moves)
    }

    @Test
    fun attackedRookSquareOnTheQueenSideDoesNotMatter() {
        // Only the squares the king crosses must be safe; b1 is attacked but the king never crosses it.
        val moves = boardOf("kr6/8/8/8/8/8/8/R3K2R", castlingRights = whiteRights).movesFrom("e1")
        assertTrue(sq("c1") in moves)
    }

    @Test
    fun deniedWithAPieceInBetween() {
        // The b1 knight blocks the queen side only.
        val moves = boardOf("4k3/8/8/8/8/8/8/RN2K2R", castlingRights = whiteRights).movesFrom("e1")
        assertFalse(sq("c1") in moves)
        assertTrue(sq("g1") in moves)
    }

    @Test
    fun deniedWithoutTheRight() {
        val moves = boardOf("4k3/8/8/8/8/8/8/R3K2R", castlingRights = CastlingRights.None).movesFrom("e1")
        assertFalse(sq("g1") in moves)
        assertFalse(sq("c1") in moves)
    }

    @Test
    fun rightsLostOnceTheKingHasMoved() {
        val board = boardOf("4k3/8/8/8/8/8/8/R3K2R", castlingRights = whiteRights)
            .playAll("e1f1", "e8d8", "f1e1", "d8e8")

        assertEquals(CastlingRights.None, board.castlingRights)
        val moves = board.movesFrom("e1")
        assertFalse(sq("g1") in moves)
        assertFalse(sq("c1") in moves)
    }

    @Test
    fun rightLostOnceTheRookHasMoved() {
        val board = boardOf("4k3/8/8/8/8/8/8/R3K2R", castlingRights = whiteRights)
            .playAll("h1h2", "e8d8", "h2h1", "d8e8")

        assertFalse(board.castlingRights.whiteKingSide)
        assertTrue(board.castlingRights.whiteQueenSide)
        val moves = board.movesFrom("e1")
        assertFalse(sq("g1") in moves)
        assertTrue(sq("c1") in moves)
    }

    @Test
    fun rightLostWhenTheRookIsCapturedOnItsCorner() {
        val board = boardOf(
            "4k3/8/8/8/8/8/6b1/R3K2R",
            playerTurn = PieceColor.Black,
            castlingRights = whiteRights
        ).play("g2", "h1")

        assertFalse(board.castlingRights.whiteKingSide)
        assertTrue(board.castlingRights.whiteQueenSide)
    }
}
