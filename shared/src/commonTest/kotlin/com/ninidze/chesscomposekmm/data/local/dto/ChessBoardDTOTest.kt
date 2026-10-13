package com.ninidze.chesscomposekmm.data.local.dto

import com.ninidze.chesscomposekmm.domain.model.CastlingRights
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.DrawReason
import com.ninidze.chesscomposekmm.domain.model.GameResult
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.testutil.START_FEN
import com.ninidze.chesscomposekmm.testutil.boardOf
import com.ninidze.chesscomposekmm.testutil.playAll
import com.ninidze.chesscomposekmm.testutil.toFen
import com.ninidze.chesscomposekmm.util.extensions.positionKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json

class ChessBoardDTOTest {

    // The same settings as ChessGameRepository.
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun startPositionRoundTrips() {
        val board = ChessBoard.createInitialChessBoard()
        val decoded = roundTrip(board)

        assertEquals(START_FEN, decoded.toFen())
        assertEquals(GameResult.Ongoing, decoded.result)
        assertEquals(board.positionHistory, decoded.positionHistory)
    }

    @Test
    fun gameStateRoundTrips() {
        // White has lost its castling rights, Black's double push left an en-passant square.
        val board = ChessBoard.createInitialChessBoard()
            .playAll("e2e4", "a7a6", "e1e2", "d7d5")
        val decoded = roundTrip(board)

        assertEquals(board.toFen(), decoded.toFen())
        assertEquals(CastlingRights(blackKingSide = true, blackQueenSide = true), decoded.castlingRights)
        assertEquals(board.enPassantTarget, decoded.enPassantTarget)
        assertEquals(board.halfmoveClock, decoded.halfmoveClock)
        assertEquals(board.fullmoveNumber, decoded.fullmoveNumber)
        assertEquals(board.positionHistory, decoded.positionHistory)
    }

    @Test
    fun everyResultRoundTrips() {
        val results = listOf(
            GameResult.Ongoing,
            GameResult.Checkmate(winner = PieceColor.White),
            GameResult.Checkmate(winner = PieceColor.Black),
            GameResult.Stalemate,
            GameResult.Draw(DrawReason.InsufficientMaterial),
            GameResult.Draw(DrawReason.FiftyMoveRule),
            GameResult.Draw(DrawReason.ThreefoldRepetition)
        )
        val board = ChessBoard.createInitialChessBoard()
        results.forEach { result ->
            assertEquals(result, roundTrip(board.copy(result = result)).result)
        }
    }

    @Test
    fun newBuildsDoNotWriteTheLegacyWinner() {
        val board = ChessBoard.createInitialChessBoard()
            .copy(result = GameResult.Checkmate(winner = PieceColor.White))
        assertNull(ChessBoardDTO.fromDomainModel(board).winner)
    }

    @Test
    fun oldJsonWithoutTheNewFieldsDecodes() {
        val decoded = decode(legacyJson(ChessBoard.createInitialChessBoard(), playerTurn = "White"))

        assertEquals(START_FEN, decoded.toFen())
        assertEquals(GameResult.Ongoing, decoded.result)
        // Kings and rooks on their home squares: the rights are inferred.
        assertEquals(CastlingRights.All, decoded.castlingRights)
        assertNull(decoded.enPassantTarget)
        assertEquals(0, decoded.halfmoveClock)
        assertEquals(1, decoded.fullmoveNumber)
        assertEquals(listOf(decoded.positionKey()), decoded.positionHistory)
    }

    @Test
    fun oldJsonLegacyWinnerIsTheCheckmatedSide() {
        // Older builds stored the side that was mated in "winner".
        val decoded = decode(
            legacyJson(ChessBoard.createInitialChessBoard(), playerTurn = "White", extra = "\"winner\":\"White\"")
        )
        assertEquals(GameResult.Checkmate(winner = PieceColor.Black), decoded.result)
    }

    @Test
    fun oldJsonInfersOnlyTheRightsWhosePiecesAreHome() {
        val board = boardOf("r3k3/8/8/8/8/8/8/4K2R")
        val decoded = decode(legacyJson(board, playerTurn = "Black"))
        assertEquals(CastlingRights(whiteKingSide = true, blackQueenSide = true), decoded.castlingRights)
        assertEquals(PieceColor.Black, decoded.playerTurn)
    }

    @Test
    fun unknownKeysAreIgnored() {
        val decoded = decode(
            legacyJson(ChessBoard.createInitialChessBoard(), playerTurn = "White", extra = "\"futureField\":42")
        )
        assertEquals(START_FEN, decoded.toFen())
    }

    @Test
    fun implausibleEnPassantSquareIsDropped() {
        // With White to move, a valid target lies on rank 6 (row 2); row 4 cannot be one.
        val decoded = decode(
            legacyJson(
                ChessBoard.createInitialChessBoard(),
                playerTurn = "White",
                extra = "\"enPassantTarget\":{\"row\":4,\"column\":3}"
            )
        )
        assertNull(decoded.enPassantTarget)
    }

    @Test
    fun negativeCountersAreClamped() {
        val decoded = decode(
            legacyJson(
                ChessBoard.createInitialChessBoard(),
                playerTurn = "White",
                extra = "\"halfmoveClock\":-5,\"fullmoveNumber\":0"
            )
        )
        assertEquals(0, decoded.halfmoveClock)
        assertEquals(1, decoded.fullmoveNumber)
    }

    @Test
    fun checkmateWithoutAWinnerFallsBackToOngoing() {
        val decoded = decode(
            legacyJson(
                ChessBoard.createInitialChessBoard(),
                playerTurn = "White",
                extra = "\"result\":{\"type\":\"Checkmate\"}"
            )
        )
        assertEquals(GameResult.Ongoing, decoded.result)
    }

    private fun roundTrip(board: ChessBoard): ChessBoard {
        val encoded = json.encodeToString(ChessBoardDTO.serializer(), ChessBoardDTO.fromDomainModel(board))
        return decode(encoded)
    }

    private fun decode(encoded: String): ChessBoard =
        json.decodeFromString(ChessBoardDTO.serializer(), encoded).toDomainModel()

    /**
     * JSON in the format of the first release: only "cells" and "playerTurn", followed by [extra],
     * further members written as JSON (without the leading comma).
     */
    private fun legacyJson(board: ChessBoard, playerTurn: String, extra: String? = null): String {
        val cells = board.cells.map { row -> row.map { piece -> piece?.let { ChessPieceDTO.fromDomainModel(it) } } }
        val cellsJson = json.encodeToString(cellsSerializer, cells)
        val extraMembers = extra?.let { ",$it" }.orEmpty()
        return "{\"cells\":$cellsJson,\"playerTurn\":\"$playerTurn\"$extraMembers}"
    }

    private val cellsSerializer = ListSerializer(ListSerializer(ChessPieceDTO.serializer().nullable))
}
