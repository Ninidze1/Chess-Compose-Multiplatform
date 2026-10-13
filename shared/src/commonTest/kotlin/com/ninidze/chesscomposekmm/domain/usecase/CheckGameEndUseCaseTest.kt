package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.DrawReason
import com.ninidze.chesscomposekmm.domain.model.GameResult
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.result.Resource
import com.ninidze.chesscomposekmm.testutil.boardOf
import com.ninidze.chesscomposekmm.testutil.play
import com.ninidze.chesscomposekmm.testutil.playAll
import com.ninidze.chesscomposekmm.testutil.sq
import com.ninidze.chesscomposekmm.util.Constants.GAME_ALREADY_FINISHED
import com.ninidze.chesscomposekmm.util.extensions.isKingChecked
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CheckGameEndUseCaseTest {

    private val checkGameEnd = CheckGameEndUseCase()

    @Test
    fun startPositionIsOngoing() {
        assertEquals(GameResult.Ongoing, checkGameEnd(ChessBoard.createInitialChessBoard()))
    }

    @Test
    fun foolsMateIsCheckmateForBlack() {
        val board = ChessBoard.createInitialChessBoard().playAll("f2f3", "e7e5", "g2g4", "d8h4")

        assertEquals(GameResult.Checkmate(winner = PieceColor.Black), board.result)
        assertEquals(PieceColor.Black, board.winner)
        assertTrue(board.isGameOver)
        assertEquals(PieceColor.White, board.playerTurn)
        assertTrue(board.isKingChecked(PieceColor.White))
    }

    @Test
    fun scholarsMateIsCheckmateForWhite() {
        val board = ChessBoard.createInitialChessBoard()
            .playAll("e2e4", "e7e5", "f1c4", "b8c6", "d1h5", "g8f6", "h5f7")

        assertEquals(GameResult.Checkmate(winner = PieceColor.White), board.result)
        assertEquals(PieceColor.White, board.winner)
    }

    @Test
    fun noMoveIsAcceptedAfterCheckmate() {
        val board = ChessBoard.createInitialChessBoard().playAll("f2f3", "e7e5", "g2g4", "d8h4")
        val piece = board.getPieceAtPosition(sq("a2"))!!

        val result = MovePieceUseCase(checkGameEnd)(board, piece, sq("a3"))

        assertEquals(Resource.Failure(GAME_ALREADY_FINISHED), result)
    }

    @Test
    fun checkWithAnEscapeIsNotMate() {
        // The e8 rook checks the king, which can step aside.
        val board = boardOf("k3r3/8/8/8/8/8/8/4K3")
        assertEquals(GameResult.Ongoing, checkGameEnd(board))
    }

    @Test
    fun stalemateIsADrawWithoutAWinner() {
        // After Qb6 the a8 king is not in check but has no legal move.
        val board = boardOf("k7/8/8/8/8/8/8/1Q5K").play("b1", "b6")

        assertEquals(GameResult.Stalemate, board.result)
        assertNull(board.winner)
        assertTrue(board.isGameOver)
        assertFalse(board.isKingChecked(PieceColor.Black))
    }

    @Test
    fun kingAgainstKingIsInsufficientMaterial() {
        assertDraw(DrawReason.InsufficientMaterial, boardOf("4k3/8/8/8/8/8/8/4K3"))
    }

    @Test
    fun kingAndKnightAgainstKingIsInsufficientMaterial() {
        assertDraw(DrawReason.InsufficientMaterial, boardOf("4k3/8/8/8/8/8/8/4KN2"))
    }

    @Test
    fun kingAndBishopAgainstKingIsInsufficientMaterial() {
        assertDraw(DrawReason.InsufficientMaterial, boardOf("4k3/8/8/8/8/8/8/2B1K3"))
    }

    @Test
    fun bishopsOnSquaresOfOneColorAreInsufficientMaterial() {
        // c1 and f8 are both dark squares.
        assertDraw(DrawReason.InsufficientMaterial, boardOf("4kb2/8/8/8/8/8/8/2B1K3"))
    }

    @Test
    fun bishopsOnSquaresOfBothColorsCanStillMate() {
        // c1 is a dark square, c8 a light one.
        assertEquals(GameResult.Ongoing, checkGameEnd(boardOf("2b1k3/8/8/8/8/8/8/2B1K3")))
    }

    @Test
    fun pawnRookOrTwoKnightsAreSufficientMaterial() {
        assertEquals(GameResult.Ongoing, checkGameEnd(boardOf("4k3/8/8/8/8/8/4P3/4K3")))
        assertEquals(GameResult.Ongoing, checkGameEnd(boardOf("4k3/8/8/8/8/8/8/R3K3")))
        assertEquals(GameResult.Ongoing, checkGameEnd(boardOf("4k3/8/8/8/8/8/8/1N2KN2")))
    }

    @Test
    fun capturingTheLastPieceDrawsByInsufficientMaterial() {
        val board = boardOf("4k3/8/8/8/8/8/3n4/4K3").play("e1", "d2")
        assertEquals(GameResult.Draw(DrawReason.InsufficientMaterial), board.result)
    }

    @Test
    fun fiftyMoveRuleDrawsAtOneHundredHalfmoves() {
        val start = boardOf("4k3/8/8/8/8/8/8/R3K3", halfmoveClock = 98)

        val afterWhite = start.play("a1", "a2")
        assertEquals(99, afterWhite.halfmoveClock)
        assertEquals(GameResult.Ongoing, afterWhite.result)

        val afterBlack = afterWhite.play("e8", "d8")
        assertEquals(100, afterBlack.halfmoveClock)
        assertEquals(GameResult.Draw(DrawReason.FiftyMoveRule), afterBlack.result)
    }

    @Test
    fun pawnMoveResetsTheFiftyMoveCounter() {
        val board = boardOf("4k3/8/8/8/8/8/4P3/R3K3", halfmoveClock = 99).play("e2", "e3")
        assertEquals(0, board.halfmoveClock)
        assertEquals(GameResult.Ongoing, board.result)
    }

    @Test
    fun threefoldRepetitionDrawsOnTheThirdOccurrence() {
        val shuffle = arrayOf("g1f3", "g8f6", "f3g1", "f6g8")
        val twice = ChessBoard.createInitialChessBoard().playAll(*shuffle)
        assertEquals(GameResult.Ongoing, twice.result)

        val almost = twice.playAll("g1f3", "g8f6", "f3g1")
        assertEquals(GameResult.Ongoing, almost.result)

        val thrice = almost.play("f6", "g8")
        assertEquals(GameResult.Draw(DrawReason.ThreefoldRepetition), thrice.result)
    }

    @Test
    fun lostCastlingRightsMakeADifferentPosition() {
        // The kings walk away and back: the pieces repeat, but the castling rights do not.
        val board = ChessBoard.createInitialChessBoard()
            .playAll("e2e4", "e7e5")
            .playAll("e1e2", "e8e7", "e2e1", "e7e8")
            .playAll("e1e2", "e8e7", "e2e1", "e7e8")
        // Without the rights, the position after e7e8 has only occurred twice.
        assertEquals(GameResult.Ongoing, board.result)
    }

    private fun assertDraw(reason: DrawReason, board: ChessBoard) {
        assertEquals(GameResult.Draw(reason), checkGameEnd(board))
    }
}
