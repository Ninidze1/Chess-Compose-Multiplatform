package com.ninidze.chesscomposekmm.data.ai

import com.ninidze.chesscomposekmm.domain.ai.ChessAi
import com.ninidze.chesscomposekmm.domain.engine.Move
import com.ninidze.chesscomposekmm.domain.engine.bitboard.BitboardUtils
import com.ninidze.chesscomposekmm.domain.engine.search.SearchEngine
import com.ninidze.chesscomposekmm.domain.engine.search.SearchParameters
import com.ninidze.chesscomposekmm.domain.model.AiMove
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.util.Constants.BOARD_SIZE
import com.ninidze.chesscomposekmm.util.Constants.ENGINE_NO_MOVE
import com.ninidze.chesscomposekmm.util.Constants.ENGINE_SEARCH_FAILED
import com.ninidze.chesscomposekmm.util.FenConverter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * [ChessAi] backed by the ported [SearchEngine].
 *
 * The engine is created lazily on [dispatcher] on the first search, so neither its allocation
 * nor the search itself runs on the main thread. The engine is mutable shared state, so every
 * search holds [mutex].
 *
 * The search is bounded by [moveTimeMs], and cancelling the caller stops it early.
 *
 * @param engineFactory Creates the engine. Called on [dispatcher], at most once per engine
 * instance (again only after a failed search discarded the previous one).
 */
class EngineChessAi(
    private val fenConverter: FenConverter,
    private val engineFactory: () -> SearchEngine,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val moveTimeMs: Int = DEFAULT_MOVE_TIME_MS
) : ChessAi {

    private val mutex = Mutex()

    // Guarded by mutex.
    private var engine: SearchEngine? = null

    override suspend fun findBestMove(board: ChessBoard): AiMove? = withContext(dispatcher) {
        mutex.withLock {
            // Skip the search if the caller was cancelled while waiting for the lock.
            currentCoroutineContext().ensureActive()
            val searchEngine = engine ?: engineFactory().also { engine = it }
            coroutineScope {
                // The search blocks this thread, so a sibling waits for cancellation (restart,
                // load) and stops the engine early instead of letting it use its full move time.
                val stopOnCancel = launch {
                    try {
                        awaitCancellation()
                    } finally {
                        searchEngine.stop()
                    }
                }
                try {
                    search(searchEngine, board)
                } finally {
                    stopOnCancel.cancel()
                }
            }
        }
    }

    private fun search(searchEngine: SearchEngine, board: ChessBoard): AiMove? {
        val bestMove = try {
            searchEngine.board.fen = fenConverter.convertToFen(board)
            searchEngine.go(SearchParameters[moveTimeMs])
            searchEngine.bestMove
        } catch (e: Exception) {
            // A search that throws leaves the engine marked as searching, which would make
            // every later go() a no-op returning a stale move. Start with a new engine instead.
            engine = null
            println(ENGINE_SEARCH_FAILED + e.message)
            return null
        }
        return toAiMove(bestMove).also {
            if (it == null) println(ENGINE_NO_MOVE + Move.toString(bestMove))
        }
    }

    companion object {
        const val DEFAULT_MOVE_TIME_MS = 1000
        private const val SQUARE_COUNT = 64

        /**
         * Converts an engine move to an [AiMove], or null for NONE / NULL / malformed moves.
         * Internal so the conversion can be unit-tested without running a search.
         */
        internal fun toAiMove(engineMove: Int): AiMove? {
            if (engineMove == Move.NONE || engineMove == Move.NULL_1) return null
            val from = squareIndexToPosition(Move.getFromIndex(engineMove)) ?: return null
            val to = squareIndexToPosition(Move.getToIndex(engineMove)) ?: return null
            if (from == to) return null
            val promotion = when (Move.getMoveType(engineMove)) {
                Move.TYPE_PROMOTION_QUEEN -> PieceType.Queen
                Move.TYPE_PROMOTION_ROOK -> PieceType.Rook
                Move.TYPE_PROMOTION_BISHOP -> PieceType.Bishop
                Move.TYPE_PROMOTION_KNIGHT -> PieceType.Knight
                else -> null
            }
            return AiMove(from = from, to = to, promotion = promotion)
        }

        /**
         * Maps an engine square index to an app [Position] through its algebraic name, so the
         * engine's internal index layout does not leak in here. Row 0 is rank 8.
         */
        private fun squareIndexToPosition(index: Int): Position? {
            if (index !in 0 until SQUARE_COUNT) return null
            val name = BitboardUtils.index2Algebraic(index)
            if (name.length != 2) return null
            val column = name[0].lowercaseChar() - 'a'
            val rank = name[1] - '1'
            if (column !in 0 until BOARD_SIZE || rank !in 0 until BOARD_SIZE) return null
            return Position(row = BOARD_SIZE - 1 - rank, column = column)
                .takeIf { it.isValidPosition() }
        }
    }
}
