package com.ninidze.chesscomposekmm.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

/** The app theme on both platforms: Material 3 colors plus the [BoardColors] for [darkTheme]. */
@Composable
fun ChessAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val boardColors = if (darkTheme) DarkBoardColors else LightBoardColors
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = DarkPrimary,
            onPrimary = DarkOnPrimary,
            background = boardColors.background,
            onBackground = DarkOnBackground,
            surface = DarkSurface,
            onSurface = DarkOnBackground
        )
    } else {
        lightColorScheme(
            primary = LightPrimary,
            onPrimary = LightOnPrimary,
            background = boardColors.background,
            onBackground = LightOnBackground,
            surface = LightSurface,
            onSurface = LightOnBackground
        )
    }
    val typography = Typography

    val shapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(0.dp)
    )

    CompositionLocalProvider(LocalBoardColors provides boardColors) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}
