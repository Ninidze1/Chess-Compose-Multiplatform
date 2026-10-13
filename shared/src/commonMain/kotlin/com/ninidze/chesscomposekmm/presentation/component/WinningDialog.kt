package com.ninidze.chesscomposekmm.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninidze.chesscomposekmm.domain.model.GameResult
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.presentation.Strings
import com.ninidze.chesscomposekmm.util.extensions.noRippleClickable

/**
 * Shows how a finished game ended, from the point of view of [humanColor]: a win, a loss,
 * or a draw with its reason. Shows nothing while the game is ongoing.
 *
 * Drawn as an overlay inside the screen rather than a platform dialog, so it works the
 * same on Android and iOS. Tapping outside the card, or "View board", calls [onDismiss].
 */
@Composable
fun WinningDialog(
    modifier: Modifier = Modifier,
    result: GameResult,
    humanColor: PieceColor,
    onPlayAgainClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = Strings.resultTitle(result, humanColor) ?: return
    val description = Strings.resultDescription(result) ?: return

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // The scrim is a sibling of the card, so taps on the card never dismiss it.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = SCRIM_ALPHA))
                .noRippleClickable(onClickLabel = Strings.VIEW_BOARD) { onDismiss() }
        )
        Surface(
            modifier = Modifier
                .padding(32.dp)
                .widthIn(max = DIALOG_MAX_WIDTH.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onPlayAgainClick) {
                    Text(text = Strings.PLAY_AGAIN)
                }
                TextButton(onClick = onDismiss) {
                    Text(text = Strings.VIEW_BOARD)
                }
            }
        }
    }
}

private const val SCRIM_ALPHA = 0.5f
private const val DIALOG_MAX_WIDTH = 360
