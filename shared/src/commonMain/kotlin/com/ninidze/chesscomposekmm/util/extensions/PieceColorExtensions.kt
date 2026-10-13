package com.ninidze.chesscomposekmm.util.extensions

import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceColor.Black
import com.ninidze.chesscomposekmm.domain.model.PieceColor.White

fun PieceColor.opposite(): PieceColor = if (this == White) Black else White
