package com.ninidze.chesscomposekmm.android.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninidze.chesscomposekmm.presentation.ChessBoardEvents
import com.ninidze.chesscomposekmm.presentation.ChessUiState
import com.ninidze.chesscomposekmm.presentation.ChessViewModel
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/**
 * Keeps the shared [ChessViewModel] across configuration changes. The shared ViewModel comes
 * from Koin and runs its work in this ViewModel's [viewModelScope].
 */
class AndroidChessViewModel : ViewModel(), KoinComponent {

    private val chessViewModel: ChessViewModel = get { parametersOf(viewModelScope) }

    val uiState: StateFlow<ChessUiState> = chessViewModel.uiState

    fun onEvent(event: ChessBoardEvents) {
        chessViewModel.onEvent(event)
    }

    override fun onCleared() {
        chessViewModel.clear()
    }
}
