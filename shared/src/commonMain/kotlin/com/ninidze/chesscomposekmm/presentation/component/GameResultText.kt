package com.ninidze.chesscomposekmm.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninidze.chesscomposekmm.domain.model.DrawReason
import com.ninidze.chesscomposekmm.domain.model.GameResult

/** Describes a finished game; shows nothing while the game is ongoing. */
@Composable
fun GameResultText(
    modifier: Modifier = Modifier,
    result: GameResult
) {
    val text = result.describe() ?: return
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 16.dp),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        textAlign = TextAlign.Center
    )
}

private fun GameResult.describe(): String? = when (this) {
    is GameResult.Ongoing -> null
    is GameResult.Checkmate -> "Checkmate: ${winner.name} wins"
    is GameResult.Stalemate -> "Draw by stalemate"
    is GameResult.Draw -> when (reason) {
        DrawReason.InsufficientMaterial -> "Draw by insufficient material"
        DrawReason.FiftyMoveRule -> "Draw by the 50-move rule"
        DrawReason.ThreefoldRepetition -> "Draw by threefold repetition"
    }
}
