//package com.ninidze.chesscomposekmm.presentation.component
//
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults.buttonColors
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.graphics.Color
//import com.ninidze.chesscomposekmm.platform.Strings
//import com.ninidze.chesscomposekmm.domain.model.PieceColor
//import com.ninidze.chesscomposekmm.domain.model.PieceColor.White
//import com.ninidze.chesscomposekmm.presentation.theme.BoardBlack
//import com.ninidze.chesscomposekmm.presentation.theme.BoardWhite
//import dev.icerock.moko.resources.StringResource
//
//@Composable
//fun WinningDialog(
//    winner: PieceColor,
//    onPlayAgainClick: () -> Unit
//) {
//    val dialogBackgroundColor = when (winner) {
//        White -> BoardWhite
//        else -> BoardBlack
//    }
//
//    val dialogButtonColor = when (winner) {
//        White -> BoardBlack
//        else -> BoardWhite
//    }
//
//    AlertDialog(
//        onDismissRequest = { /* Dismiss the dialog */ },
//        backgroundColor = dialogBackgroundColor,
//        title = {
//            Text(
//                text = if (winner == White)
//                    StringResource(id = Strings.whiteWins)
//                else
//                    "Black wins"
//            )
//        },
//        text = { Text(text = "${winner.name} wins by Checkmate") },
//        confirmButton = {
//            Button(
//                onClick = { onPlayAgainClick.invoke() },
//                colors = buttonColors(
//                    containerColor = dialogButtonColor,
//                )
//            ) {
//                Text(
//                    text = "Play again",
//                    color = Color.Black
//                )
//            }
//        }
//    )
//}
