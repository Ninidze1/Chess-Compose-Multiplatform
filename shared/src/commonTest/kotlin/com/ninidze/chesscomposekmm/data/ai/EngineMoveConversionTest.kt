package com.ninidze.chesscomposekmm.data.ai

import com.ninidze.chesscomposekmm.domain.engine.Move
import com.ninidze.chesscomposekmm.domain.engine.Piece
import com.ninidze.chesscomposekmm.domain.engine.bitboard.BitboardUtils
import com.ninidze.chesscomposekmm.domain.model.AiMove
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.testutil.sq
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The conversion of the engine's int-encoded moves into [AiMove]s, including the "none" and
 * "null" results the engine reports when it has no move.
 */
class EngineMoveConversionTest {

    @Test
    fun noneIsNoMove() {
        assertEquals("none", Move.toString(Move.NONE))
        assertNull(EngineChessAi.toAiMove(Move.NONE))
    }

    @Test
    fun nullMoveIsNoMove() {
        assertEquals("null", Move.toString(Move.NULL_1))
        assertNull(EngineChessAi.toAiMove(Move.NULL_1))
    }

    @Test
    fun plainMoveMapsToAppSquares() {
        val move = engineMove("e2", "e4", Piece.PAWN, moveType = 0)
        assertEquals(AiMove(from = sq("e2"), to = sq("e4")), EngineChessAi.toAiMove(move))
    }

    @Test
    fun cornersMapToTheRightRowsAndColumns() {
        val move = engineMove("a8", "h1", Piece.BISHOP, moveType = 0)
        assertEquals(AiMove(from = sq("a8"), to = sq("h1")), EngineChessAi.toAiMove(move))
    }

    @Test
    fun promotionPieceIsKept() {
        val expected = mapOf(
            Move.TYPE_PROMOTION_QUEEN to PieceType.Queen,
            Move.TYPE_PROMOTION_ROOK to PieceType.Rook,
            Move.TYPE_PROMOTION_BISHOP to PieceType.Bishop,
            Move.TYPE_PROMOTION_KNIGHT to PieceType.Knight
        )
        expected.forEach { (moveType, pieceType) ->
            val move = engineMove("b7", "b8", Piece.PAWN, moveType)
            assertEquals(
                AiMove(from = sq("b7"), to = sq("b8"), promotion = pieceType),
                EngineChessAi.toAiMove(move)
            )
        }
    }

    @Test
    fun castlingHasNoPromotion() {
        val move = engineMove("e1", "g1", Piece.KING, Move.TYPE_KINGSIDE_CASTLING)
        assertEquals(AiMove(from = sq("e1"), to = sq("g1")), EngineChessAi.toAiMove(move))
    }

    @Test
    fun moveOntoItsOwnSquareIsRejected() {
        val move = engineMove("d4", "d4", Piece.QUEEN, moveType = 0)
        assertNull(EngineChessAi.toAiMove(move))
    }

    private fun engineMove(from: String, to: String, piece: Int, moveType: Int): Int = Move.genMove(
        BitboardUtils.algebraic2Index(from),
        BitboardUtils.algebraic2Index(to),
        piece,
        false,
        moveType
    )
}
