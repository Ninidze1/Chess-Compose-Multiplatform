package com.ninidze.chesscomposekmm.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninidze.chesscomposekmm.presentation.Strings
import com.ninidze.chesscomposekmm.presentation.theme.LocalBoardColors
import com.ninidze.chesscomposekmm.resources.Res
import com.ninidze.chesscomposekmm.resources.ic_restart
import org.jetbrains.compose.resources.painterResource

@Composable
fun RestartButton(
    modifier: Modifier = Modifier,
    isVisible: Boolean = false,
    onRestartClick: () -> Unit
) {
    AnimatedVisibility(visible = isVisible) {
        Button(
            modifier = modifier
                .height(75.dp)
                .aspectRatio(1f),
            onClick = onRestartClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalBoardColors.current.lightSquare
            )
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_restart),
                contentDescription = Strings.RESTART
            )
        }
    }
}
