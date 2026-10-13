package com.ninidze.chesscomposekmm.domain.rules

import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.testutil.boardOf
import com.ninidze.chesscomposekmm.testutil.play
import com.ninidze.chesscomposekmm.testutil.sq
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PromotionTest {

    // White pawn on a7 about to promote; the kings are far away.
    private val whiteToPromote = boardOf("8/P6k/8/8/8/8/8/K7")

    @Test
    fun defaultsToAQueen() {
        val board = whiteToPromote.play("a7", "a8")
        assertEquals(PieceType.Queen, board.getPieceAtPosition(sq("a8"))?.type)
        assertNull(board.getPieceAtPosition(sq("a7")))
    }

    @Test
    fun honoursTheChosenPiece() {
        listOf(PieceType.Queen, PieceType.Rook, PieceType.Bishop, PieceType.Knight).forEach { choice ->
            val promoted = whiteToPromote.play("a7", "a8", promotion = choice).getPieceAtPosition(sq("a8"))
            assertEquals(choice, promoted?.type, "Promotion to $choice")
            assertEquals(PieceColor.White, promoted?.color)
        }
    }

    @Test
    fun kingOrPawnIsNeverAPromotionTarget() {
        listOf(PieceType.King, PieceType.Pawn).forEach { choice ->
            val promoted = whiteToPromote.play("a7", "a8", promotion = choice).getPieceAtPosition(sq("a8"))
            assertEquals(PieceType.Queen, promoted?.type, "Promotion to $choice")
        }
    }

    @Test
    fun promotesWithACapture() {
        val board = boardOf("1r5k/P7/8/8/8/8/8/K7").play("a7", "b8", promotion = PieceType.Knight)
        assertEquals(PieceType.Knight, board.getPieceAtPosition(sq("b8"))?.type)
        assertEquals(PieceColor.White, board.getPieceAtPosition(sq("b8"))?.color)
        assertEquals(0, board.halfmoveClock)
    }

    @Test
    fun blackPromotesOnTheFirstRank() {
        val board = boardOf("k7/8/8/8/8/8/7p/K7", playerTurn = PieceColor.Black)
            .play("h2", "h1", promotion = PieceType.Rook)
        assertEquals(PieceType.Rook, board.getPieceAtPosition(sq("h1"))?.type)
        assertEquals(PieceColor.Black, board.getPieceAtPosition(sq("h1"))?.color)
    }

    @Test
    fun promotionChoiceIsIgnoredBeforeTheLastRank() {
        val board = boardOf("k7/8/8/8/8/8/4P3/K7").play("e2", "e4", promotion = PieceType.Knight)
        assertEquals(PieceType.Pawn, board.getPieceAtPosition(sq("e4"))?.type)
    }
}
