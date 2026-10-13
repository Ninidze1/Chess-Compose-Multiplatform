package com.ninidze.chesscomposekmm.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninidze.chesscomposekmm.domain.model.GameResult
import com.ninidze.chesscomposekmm.presentation.Strings

/** Describes a finished game; shows nothing while the game is ongoing. */
@Composable
fun GameResultText(modifier: Modifier = Modifier, result: GameResult) {
    val text = Strings.resultDescription(result) ?: return
    Text(
        text = text,
        modifier = modifier
            .padding(horizontal = 16.dp)
            // Announced by screen readers when the game ends.
            .semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )
}
