package com.ninidze.chesscomposekmm.presentation

import com.ninidze.chesscomposekmm.domain.ai.ChessAi
import com.ninidze.chesscomposekmm.domain.model.AiMove
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.GameResult
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.movement.ActionType
import com.ninidze.chesscomposekmm.domain.usecase.CheckGameEndUseCase
import com.ninidze.chesscomposekmm.domain.usecase.LoadBoardUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MoveAIUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import com.ninidze.chesscomposekmm.domain.usecase.SaveBoardUseCase
import com.ninidze.chesscomposekmm.domain.usecase.StartGameUseCase
import com.ninidze.chesscomposekmm.testutil.FakeChessAi
import com.ninidze.chesscomposekmm.testutil.FakeChessGameRepository
import com.ninidze.chesscomposekmm.testutil.FakeSoundPlayer
import com.ninidze.chesscomposekmm.testutil.START_FEN
import com.ninidze.chesscomposekmm.testutil.play
import com.ninidze.chesscomposekmm.testutil.playAll
import com.ninidze.chesscomposekmm.testutil.sq
import com.ninidze.chesscomposekmm.testutil.toFen
import com.ninidze.chesscomposekmm.util.Constants.AI_MOVE_DELAY_MS
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

/**
 * The turn flow of [ChessViewModel] (the human plays White, the AI Black) with fake AI,
 * sound and storage, on a [StandardTestDispatcher] whose virtual clock the tests drive.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChessViewModelTest {

    private val sounds = FakeSoundPlayer()
    private val repository = FakeChessGameRepository()

    private val start = ChessBoard.createInitialChessBoard()
    private val afterE4 = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
    private val afterE4E5 = "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2"
    private val blackE7E5 = AiMove(from = sq("e7"), to = sq("e5"))

    // Just past the pause before the AI starts searching.
    private val pastAiDelay = (AI_MOVE_DELAY_MS + 1).milliseconds

    // White to move, Qh5xf7 is mate.
    private val beforeScholarsMate = start.playAll("e2e4", "e7e5", "f1c4", "b8c6", "d1h5", "g8f6")

    @Test
    fun startsANewGameWhenNothingIsSaved() = viewModelTest {
        val viewModel = viewModel(FakeChessAi())
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertFalse(state.isInputLocked)
        assertEquals(START_FEN, state.board.toFen())
        assertEquals(listOf(ActionType.START), sounds.played)
    }

    @Test
    fun aiAnswersTheHumanMove() = viewModelTest {
        val ai = FakeChessAi { blackE7E5 }
        val viewModel = viewModel(ai)

        viewModel.onEvent(ChessBoardEvents.OnPieceMove(sq("e2"), sq("e4")))
        // The human move is applied before onEvent returns, and the AI's turn has begun.
        with(viewModel.uiState.value) {
            assertEquals(afterE4, board.toFen())
            assertTrue(isAiThinking)
            assertTrue(isInputLocked)
        }

        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertEquals(afterE4E5, board.toFen())
            assertFalse(isAiThinking)
            assertNull(aiMove)
            assertFalse(isInputLocked)
        }
        assertEquals(1, ai.requests.size)
        assertEquals(afterE4, ai.requests.single().toFen())
        assertEquals(afterE4E5, repository.savedBoard?.toFen())
    }

    @Test
    fun aiMoveIsAppliedWhenTheAnimationFinishes() = viewModelTest {
        val viewModel = viewModel(FakeChessAi { blackE7E5 })

        viewModel.onEvent(ChessBoardEvents.OnPieceMove(sq("e2"), sq("e4")))
        advanceTimeBy(pastAiDelay)

        // The move is published for the UI to animate, but not applied yet.
        assertEquals(blackE7E5, viewModel.uiState.value.aiMove)
        assertEquals(afterE4, viewModel.uiState.value.board.toFen())

        viewModel.onEvent(ChessBoardEvents.AiMoveFinished(blackE7E5.from, blackE7E5.to))
        runCurrent()

        assertEquals(afterE4E5, viewModel.uiState.value.board.toFen())
        assertNull(viewModel.uiState.value.aiMove)
    }

    @Test
    fun humanCannotMoveDuringTheAiTurn() = viewModelTest {
        val searchMayFinish = CompletableDeferred<Unit>()
        val ai = FakeChessAi {
            searchMayFinish.await()
            blackE7E5
        }
        val viewModel = viewModel(ai)

        viewModel.onEvent(ChessBoardEvents.OnPieceMove(sq("e2"), sq("e4")))
        // While the AI waits before searching...
        viewModel.onEvent(ChessBoardEvents.OnPieceMove(sq("d2"), sq("d4")))
        assertEquals(afterE4, viewModel.uiState.value.board.toFen())

        // ...and while it searches, neither White's nor the AI's pieces can be moved.
        advanceTimeBy(pastAiDelay)
        assertEquals(1, ai.requests.size)
        viewModel.onEvent(ChessBoardEvents.OnPieceMove(sq("d2"), sq("d4")))
        viewModel.onEvent(ChessBoardEvents.OnPieceMove(sq("e7"), sq("e6")))
        runCurrent()
        assertEquals(afterE4, viewModel.uiState.value.board.toFen())

        searchMayFinish.complete(Unit)
        advanceUntilIdle()

        assertEquals(afterE4E5, viewModel.uiState.value.board.toFen())
        assertEquals(1, ai.requests.size)
    }

    @Test
    fun humanCannotMoveTheAiPieces() = viewModelTest {
        val ai = FakeChessAi { blackE7E5 }
        val viewModel = viewModel(ai)

        viewModel.onEvent(ChessBoardEvents.OnPieceMove(sq("e7"), sq("e5")))
        advanceUntilIdle()

        assertEquals(START_FEN, viewModel.uiState.value.board.toFen())
        assertTrue(ai.requests.isEmpty())
    }

    @Test
    fun aiNeverMovesAfterTheHumanDeliversMate() = viewModelTest {
        repository.savedBoard = beforeScholarsMate
        val ai = FakeChessAi { AiMove(from = sq("a7"), to = sq("a6")) }
        val viewModel = viewModel(ai)

        viewModel.onEvent(ChessBoardEvents.OnPieceMove(sq("h5"), sq("f7")))
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertEquals(GameResult.Checkmate(winner = PieceColor.White), board.result)
            assertEquals(PieceColor.Black, board.playerTurn)
            assertFalse(isAiThinking)
            assertNull(aiMove)
            assertTrue(isInputLocked)
        }
        assertTrue(ai.requests.isEmpty())
        assertEquals(ActionType.FINISH, sounds.played.last())
    }

    @Test
    fun aiNeverMovesInALoadedFinishedGame() = viewModelTest {
        repository.savedBoard = beforeScholarsMate.play("h5", "f7")
        val ai = FakeChessAi { AiMove(from = sq("a7"), to = sq("a6")) }
        val viewModel = viewModel(ai)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.board.isGameOver)
        assertTrue(viewModel.uiState.value.isInputLocked)
        assertTrue(ai.requests.isEmpty())
    }

    @Test
    fun noMoveIsAcceptedAfterTheHumanIsMated() = viewModelTest {
        val foolsMate = start.playAll("f2f3", "e7e5", "g2g4", "d8h4")
        repository.savedBoard = foolsMate
        val ai = FakeChessAi { blackE7E5 }
        val viewModel = viewModel(ai)

        viewModel.onEvent(ChessBoardEvents.OnPieceMove(sq("a2"), sq("a3")))
        advanceUntilIdle()

        assertEquals(foolsMate.toFen(), viewModel.uiState.value.board.toFen())
        assertTrue(ai.requests.isEmpty())
    }

    @Test
    fun loadedGameWithTheAiToMoveResumesWithTheAi() = viewModelTest {
        repository.savedBoard = start.play("e2", "e4")
        val ai = FakeChessAi { blackE7E5 }
        val viewModel = viewModel(ai)

        assertTrue(viewModel.uiState.value.isInputLocked)
        advanceUntilIdle()

        assertEquals(afterE4E5, viewModel.uiState.value.board.toFen())
        assertEquals(1, ai.requests.size)
    }

    @Test
    fun illegalAiMoveIsReplacedByALegalOne() = viewModelTest {
        // The AI tries to move a White rook.
        val ai = FakeChessAi { AiMove(from = sq("a1"), to = sq("a3")) }
        val viewModel = viewModel(ai)

        viewModel.onEvent(ChessBoardEvents.OnPieceMove(sq("e2"), sq("e4")))
        advanceUntilIdle()

        with(viewModel.uiState.value.board) {
            assertEquals(PieceColor.White, playerTurn)
            assertNotNull(getPieceAtPosition(sq("a1")))
        }
    }

    @Test
    fun restartCancelsAPendingAiSearch() = viewModelTest {
        val searchMayFinish = CompletableDeferred<Unit>()
        val ai = FakeChessAi {
            searchMayFinish.await()
            blackE7E5
        }
        val viewModel = viewModel(ai)

        viewModel.onEvent(ChessBoardEvents.OnPieceMove(sq("e2"), sq("e4")))
        advanceTimeBy(pastAiDelay)
        assertEquals(1, ai.requests.size)

        viewModel.onEvent(ChessBoardEvents.OnGameRestart)
        searchMayFinish.complete(Unit)
        advanceUntilIdle()

        assertRestarted(viewModel)
        assertEquals(1, ai.requests.size)
    }

    @Test
    fun restartCancelsAPendingAiAnimation() = viewModelTest {
        val ai = FakeChessAi { blackE7E5 }
        val viewModel = viewModel(ai)

        viewModel.onEvent(ChessBoardEvents.OnPieceMove(sq("e2"), sq("e4")))
        advanceTimeBy(pastAiDelay)
        assertEquals(blackE7E5, viewModel.uiState.value.aiMove)

        viewModel.onEvent(ChessBoardEvents.OnGameRestart)
        // A late animation callback for the cancelled move must not apply it either.
        viewModel.onEvent(ChessBoardEvents.AiMoveFinished(blackE7E5.from, blackE7E5.to))
        advanceUntilIdle()

        assertRestarted(viewModel)
        assertEquals(1, ai.requests.size)
    }

    @Test
    fun restartBeforeTheAiDelayEndsSkipsTheSearch() = viewModelTest {
        val ai = FakeChessAi { blackE7E5 }
        val viewModel = viewModel(ai)

        viewModel.onEvent(ChessBoardEvents.OnPieceMove(sq("e2"), sq("e4")))
        viewModel.onEvent(ChessBoardEvents.OnGameRestart)
        advanceUntilIdle()

        assertRestarted(viewModel)
        assertTrue(ai.requests.isEmpty())
    }

    private fun assertRestarted(viewModel: ChessViewModel) {
        with(viewModel.uiState.value) {
            assertEquals(START_FEN, board.toFen())
            assertNull(aiMove)
            assertFalse(isAiThinking)
            assertFalse(isInputLocked)
        }
        assertEquals(START_FEN, repository.savedBoard?.toFen())
        assertEquals(ActionType.START, sounds.played.last())
    }

    private fun viewModelTest(body: suspend TestScope.() -> Unit): TestResult =
        runTest(StandardTestDispatcher()) { body() }

    /**
     * A ViewModel running in the test's [TestScope.backgroundScope], with the saved game
     * already loaded.
     */
    private fun TestScope.viewModel(ai: ChessAi): ChessViewModel {
        val viewModel = ChessViewModel(
            movePieceUseCase = MovePieceUseCase(CheckGameEndUseCase()),
            soundPlayer = sounds,
            calculateBotMoveUseCase = MoveAIUseCase(ai),
            startGameUseCase = StartGameUseCase(),
            saveBoardUseCase = SaveBoardUseCase(repository),
            loadBoardUseCase = LoadBoardUseCase(repository),
            coroutineScope = backgroundScope
        )
        runCurrent()
        return viewModel
    }
}
