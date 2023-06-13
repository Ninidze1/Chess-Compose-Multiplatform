package com.ninidze.chesscomposekmm.android.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ninidze.chesscomposekmm.android.presentation.AndroidChessViewModel
import com.ninidze.chesscomposekmm.presentation.BoardScreen
import com.ninidze.chesscomposekmm.presentation.theme.BackgroundColor
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = BackgroundColor,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val viewModel by viewModel<AndroidChessViewModel>()
                    val state by viewModel.chessBoardState.collectAsStateWithLifecycle()
                    BoardScreen(
                        chessBoardState = state,
                        chessBoardEvents = viewModel::onEvent
                    )
                }
            }
        }
    }
}

//@Composable
//fun stringResource(id: StringResource, vararg args: Any): String {
//    return BiometricManager.Strings(LocalContext.current).get(id, args.toList())
//}