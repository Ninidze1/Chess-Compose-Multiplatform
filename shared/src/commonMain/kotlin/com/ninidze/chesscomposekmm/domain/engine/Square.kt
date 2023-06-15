package com.ninidze.chesscomposekmm.domain.engine

object Square {
    const val ALL: Long = -1
    const val WHITES: Long = -6172840429334713771
    const val BLACKS: Long = 6172840429334713770

    const val H1 = 1L
    val G1 = H1 shl 1
    val B1 = H1 shl 6
    val A1 = H1 shl 7

    val H2 = H1 shl 8
    val G2 = H1 shl 9
    val F2 = H1 shl 10
    val C2 = H1 shl 13
    val B2 = H1 shl 14
    val A2 = H1 shl 15

    val G3 = H1 shl 17
    val F3 = H1 shl 18
    val C3 = H1 shl 21
    val B3 = H1 shl 22

    val G4 = H1 shl 25
    val B4 = H1 shl 30

    val G5 = H1 shl 33
    val B5 = H1 shl 38

    val G6 = H1 shl 41
    val F6 = H1 shl 42
    val C6 = H1 shl 45
    val B6 = H1 shl 46

    val H7 = H1 shl 48
    val G7 = H1 shl 49
    val F7 = H1 shl 50
    val C7 = H1 shl 53
    val B7 = H1 shl 54
    val A7 = H1 shl 55

    val H8 = H1 shl 56
    val G8 = H1 shl 57
    val B8 = H1 shl 62
    val A8 = H1 shl 63
}
