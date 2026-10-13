package com.ninidze.chesscomposekmm.domain.rules

import com.ninidze.chesscomposekmm.domain.model.CastlingRights
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.testutil.boardOf
import com.ninidze.chesscomposekmm.testutil.legalMoves
import com.ninidze.chesscomposekmm.testutil.movesFrom
import com.ninidze.chesscomposekmm.testutil.ongoing
import com.ninidze.chesscomposekmm.testutil.play
import com.ninidze.chesscomposekmm.testutil.sq
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Perft: the number of leaf nodes of the legal move tree to a fixed depth, compared with the
 * published reference counts (https://www.chessprogramming.org/Perft_Results).
 *
 * A promotion counts as one move here, because the app's move generator returns target
 * squares; none of the positions below reach a promotion within the tested depth.
 */
class MoveGenerationTest {

    @Test
    fun startPositionDepthOne() {
        assertEquals(20L, perft(ChessBoard.createInitialChessBoard(), depth = 1))
    }

    @Test
    fun startPositionDepthTwo() {
        assertEquals(400L, perft(ChessBoard.createInitialChessBoard(), depth = 2))
    }

    @Test
    fun startPositionDepthThree() {
        assertEquals(8902L, perft(ChessBoard.createInitialChessBoard(), depth = 3))
    }

    /** "Kiwipete": castling on both wings, en passant, pins and checks. */
    @Test
    fun kiwipeteDepthTwo() {
        val board = boardOf(
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R",
            castlingRights = CastlingRights.All
        )
        assertEquals(48L, perft(board, depth = 1))
        assertEquals(2039L, perft(board, depth = 2))
    }

    /** Chess Programming Wiki position 3: en passant along a pinned rank. */
    @Test
    fun rookEndgameDepthThree() {
        val board = boardOf("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8")
        assertEquals(14L, perft(board, depth = 1))
        assertEquals(191L, perft(board, depth = 2))
        assertEquals(2812L, perft(board, depth = 3))
    }

    @Test
    fun pinnedPieceCannotLeaveThePinLine() {
        // The e2 knight is pinned to the king by the e8 rook.
        val board = boardOf("k3r3/8/8/8/8/8/4N3/4K3")
        assertTrue(board.movesFrom("e2").isEmpty())
    }

    @Test
    fun kingInCheckMustAnswerTheCheck() {
        // The e8 rook checks the king; only king moves off the e-file or Nd1-e3 blocking it help.
        val board = boardOf("k3r3/8/8/8/8/8/8/3NK3")
        val moves = board.legalMoves().map { it.second }.toSet()
        assertEquals(setOf(sq("d2"), sq("f2"), sq("f1"), sq("e3")), moves)
    }

    @Test
    fun onlyTheSideToMoveHasMoves() {
        val board = ChessBoard.createInitialChessBoard()
        assertTrue(board.legalMoves().all { (from, _) -> board.getPieceAtPosition(from)?.color == PieceColor.White })
    }

    private fun perft(board: ChessBoard, depth: Int): Long {
        val moves = board.legalMoves()
        if (depth <= 1) return moves.size.toLong()
        // A drawn position (e.g. by repetition) still has moves for perft purposes.
        val playable = board.ongoing()
        return moves.sumOf { (from, to) -> perft(playable.play(from, to), depth - 1) }
    }
}
