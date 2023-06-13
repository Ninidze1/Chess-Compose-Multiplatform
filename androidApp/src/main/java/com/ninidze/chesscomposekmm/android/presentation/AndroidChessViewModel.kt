package com.ninidze.chesscomposekmm.android.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.presentation.ChessBoardEvents
import com.ninidze.chesscomposekmm.domain.usecase.CheckMateUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import com.ninidze.chesscomposekmm.presentation.ChessViewModel
import kotlinx.coroutines.flow.StateFlow

class AndroidChessViewModel(
    private val movePieceUseCase: MovePieceUseCase,
    private val checkmateUseCase: CheckMateUseCase,
) : ViewModel() {

    private val viewModel by lazy {
        ChessViewModel(
            movePieceUseCase = movePieceUseCase,
            checkmateUseCase = checkmateUseCase,
            coroutineScope = viewModelScope
        )
    }

    val chessBoardState: StateFlow<ChessBoard> = viewModel.chessBoardState

    fun onEvent(event: ChessBoardEvents) {
        viewModel.onEvent(event)
    }
}