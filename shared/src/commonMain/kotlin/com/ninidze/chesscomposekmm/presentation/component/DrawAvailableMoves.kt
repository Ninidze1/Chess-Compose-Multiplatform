package com.ninidze.chesscomposekmm.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ninidze.chesscomposekmm.presentation.theme.LocalBoardColors

/**
 * Marks an empty square the selected piece can move to. It draws only: the square it sits
 * on handles the tap and describes the move to accessibility services.
 */
@Composable
fun DrawAvailableMoves(modifier: Modifier = Modifier) {
    val color = LocalBoardColors.current.moveHint
    Canvas(modifier = modifier.fillMaxSize()) {
        drawCircle(
            color = color,
            radius = size.minDimension / MARKER_RADIUS_DIVISOR,
            center = center
        )
    }
}

private const val MARKER_RADIUS_DIVISOR = 7
