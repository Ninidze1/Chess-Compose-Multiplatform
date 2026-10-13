package com.ninidze.chesscomposekmm.data.repository

import com.ninidze.chesscomposekmm.data.local.dto.ChessBoardDTO
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.repository.IChessGameRepository
import com.ninidze.chesscomposekmm.util.Constants.BOARD_SIZE
import com.ninidze.chesscomposekmm.util.Constants.CHESS_BOARD_KEY
import com.ninidze.chesscomposekmm.util.Constants.ERROR_DECODING_BOARD
import com.ninidze.chesscomposekmm.util.Constants.INVALID_BOARD_SIZE
import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.serialization.json.Json

@Suppress("OPT_IN_USAGE")
class ChessGameRepository(private val dataStoreSettings: FlowSettings) : IChessGameRepository {

    override suspend fun saveBoard(board: ChessBoard) {
        val chessBoardDto = ChessBoardDTO.fromDomainModel(board)
        val encoded = json.encodeToString(ChessBoardDTO.serializer(), chessBoardDto)
        dataStoreSettings.putString(CHESS_BOARD_KEY, encoded)
    }

    override suspend fun loadBoard(): ChessBoard? {
        val saved = dataStoreSettings.getStringOrNull(CHESS_BOARD_KEY)
        if (saved.isNullOrEmpty()) return null
        return decodeBoard(saved) ?: run {
            // Drop the unreadable data so it cannot break every following launch.
            dataStoreSettings.remove(CHESS_BOARD_KEY)
            null
        }
    }

    // Returns null if [saved] is not a readable board of the expected size.
    private fun decodeBoard(saved: String): ChessBoard? {
        val board = try {
            json.decodeFromString(ChessBoardDTO.serializer(), saved).toDomainModel()
        } catch (e: Exception) {
            // Typically a SerializationException; decoding does not suspend, so this
            // cannot swallow a cancellation.
            println(ERROR_DECODING_BOARD + e.message)
            return null
        }
        val hasExpectedSize = board.cells.size == BOARD_SIZE &&
            board.cells.all { row -> row.size == BOARD_SIZE }
        if (!hasExpectedSize) {
            println(ERROR_DECODING_BOARD + INVALID_BOARD_SIZE)
            return null
        }
        return board
    }

    private companion object {
        // Tolerates fields written by other versions of the app, and falls back to a
        // property's default for values it cannot read (such as an unknown enum constant).
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}
