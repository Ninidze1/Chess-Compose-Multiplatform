package com.ninidze.chesscomposekmm.data.repository

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.testutil.playAll
import com.ninidze.chesscomposekmm.testutil.toFen
import com.ninidze.chesscomposekmm.util.Constants.CHESS_BOARD_KEY
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class ChessGameRepositoryTest {

    private val settings = MapSettings()

    private fun TestScope.repository() =
        ChessGameRepository(settings.toFlowSettings(StandardTestDispatcher(testScheduler)))

    @Test
    fun nothingSavedLoadsNull() = runTest {
        assertNull(repository().loadBoard())
    }

    @Test
    fun savedBoardLoadsBack() = runTest {
        val board = ChessBoard.createInitialChessBoard().playAll("e2e4", "e7e5", "g1f3")
        val repository = repository()

        repository.saveBoard(board)
        val loaded = assertNotNull(repository.loadBoard())

        assertEquals(board.toFen(), loaded.toFen())
        assertEquals(board.positionHistory, loaded.positionHistory)
    }

    @Test
    fun unreadableDataIsDiscarded() = runTest {
        settings.putString(CHESS_BOARD_KEY, "{not json")

        assertNull(repository().loadBoard())
        assertNull(settings.getStringOrNull(CHESS_BOARD_KEY))
    }

    @Test
    fun boardOfTheWrongSizeIsDiscarded() = runTest {
        val row = List(8) { "null" }.joinToString(prefix = "[", postfix = "]")
        val sevenRows = List(7) { row }.joinToString(prefix = "[", postfix = "]")
        settings.putString(CHESS_BOARD_KEY, "{\"cells\":$sevenRows,\"playerTurn\":\"White\"}")

        assertNull(repository().loadBoard())
        assertNull(settings.getStringOrNull(CHESS_BOARD_KEY))
    }

    @Test
    fun emptyDataLoadsNull() = runTest {
        settings.putString(CHESS_BOARD_KEY, "")
        assertNull(repository().loadBoard())
    }
}
