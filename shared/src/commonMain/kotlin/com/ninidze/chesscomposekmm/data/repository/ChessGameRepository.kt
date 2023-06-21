package com.ninidze.chesscomposekmm.data.repository

import com.ninidze.chesscomposekmm.data.helper.Resource
import com.ninidze.chesscomposekmm.data.helper.safeCall
import com.ninidze.chesscomposekmm.data.local.dto.ChessBoardDTO
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.repository.IChessGameRepository
import com.ninidze.chesscomposekmm.util.Constants.CHESS_BOARD_KEY
import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

@Suppress("OPT_IN_USAGE")
class ChessGameRepository(private val dataStoreSettings: FlowSettings) : IChessGameRepository {
    override suspend fun saveBoard(board: ChessBoard): Resource<Unit> {
        return safeCall {
            val chessBoardDto = ChessBoardDTO.fromDomainModel(board)
            val json = Json.encodeToString(ChessBoardDTO.serializer(), chessBoardDto)
            dataStoreSettings.putString(CHESS_BOARD_KEY, json)
        }
    }

    override suspend fun loadBoard(): Resource<Flow<ChessBoard>> {
        return safeCall {
            dataStoreSettings.getStringFlow(CHESS_BOARD_KEY, defaultValue = "")
                .map { json ->
                    return@map if (json.isNotEmpty()) {
                        Json.decodeFromString(ChessBoardDTO.serializer(), json).toDomainModel()
                    } else {
                        ChessBoard.createInitialChessBoard()
                    }
                }
        }
    }

}
