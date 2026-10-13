package com.ninidze.chesscomposekmm.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colors of the board and the screen around it, provided by [ChessAppTheme] through
 * [LocalBoardColors].
 *
 * @property background The screen behind the board.
 * @property lightSquare The light squares; also used for light surfaces such as the
 * promotion picker and the restart button.
 * @property darkSquare The dark squares.
 * @property highlight Drawn over the selected square and capturable pieces.
 * @property moveHint The dot on empty squares the selected piece can move to.
 */
@Immutable
data class BoardColors(
    val background: Color,
    val lightSquare: Color,
    val darkSquare: Color,
    val highlight: Color,
    val moveHint: Color
)

private val Violet = Color(0xFF7B61FF)
private val VioletLight = Color(0xFFB9ACFF)
private val InkDark = Color(0xFF1B1F2B)
private val InkLight = Color(0xFFE4E7F0)

val LightBoardColors = BoardColors(
    background = Color(0xFFD4DAEB),
    lightSquare = Color(0xFFE8EDF9),
    darkSquare = Color(0xFFB7C0D8),
    highlight = Violet.copy(alpha = 0.5f),
    moveHint = Violet.copy(alpha = 0.5f)
)

val DarkBoardColors = BoardColors(
    background = Color(0xFF161A24),
    lightSquare = Color(0xFF9AA5C4),
    darkSquare = Color(0xFF5D6886),
    highlight = VioletLight.copy(alpha = 0.6f),
    moveHint = VioletLight.copy(alpha = 0.7f)
)

val LocalBoardColors = staticCompositionLocalOf { LightBoardColors }

internal val LightPrimary = Color(0xFF5B45E0)
internal val LightOnPrimary = Color.White
internal val LightOnBackground = InkDark
internal val LightSurface = LightBoardColors.lightSquare

internal val DarkPrimary = VioletLight
internal val DarkOnPrimary = Color(0xFF2A1F6B)
internal val DarkOnBackground = InkLight
internal val DarkSurface = Color(0xFF262B3A)
