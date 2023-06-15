package com.ninidze.chesscomposekmm.data.engine.evaluation

import com.ninidze.chesscomposekmm.domain.engine.Board
import com.ninidze.chesscomposekmm.domain.engine.Color
import com.ninidze.chesscomposekmm.domain.engine.bitboard.AttacksInfo
import com.ninidze.chesscomposekmm.domain.engine.bitboard.BitboardAttacks
import com.ninidze.chesscomposekmm.domain.engine.util.StringUtils

abstract class Evaluator {

    var bbAttacks: BitboardAttacks = BitboardAttacks.getInstance()

    /**
     * Board evaluator
     */
    abstract fun evaluate(b: Board, ai: AttacksInfo): Int

    fun formatOE(value: Int): String {
        return StringUtils.padLeft(
            o(value).toString(),
            8
        ) + " " + StringUtils.padLeft(e(value).toString(), 8)
    }

    companion object {
        val W = Color.W
        val B = Color.B

        const val NO_VALUE = Short.MAX_VALUE.toInt()
        const val MATE = 30000
        const val KNOWN_WIN = 20000
        const val DRAW = 0

        private const val PAWN_OPENING = 80
        const val PAWN = 100
        const val KNIGHT = 325
        const val BISHOP = 325
        const val ROOK = 500
        const val QUEEN = 975
        val PIECE_VALUES = intArrayOf(0, PAWN, KNIGHT, BISHOP, ROOK, QUEEN)
        val PIECE_VALUES_OE = intArrayOf(
            0, oe(PAWN_OPENING, PAWN), oe(KNIGHT, KNIGHT), oe(BISHOP, BISHOP), oe(
                ROOK, ROOK
            ), oe(QUEEN, QUEEN)
        )
        val BISHOP_PAIR = oe(50, 50) // Bonus by having two bishops in different colors

        const val GAME_PHASE_MIDGAME = 1000
        const val GAME_PHASE_ENDGAME = 0
        const val NON_PAWN_MATERIAL_ENDGAME_MIN = QUEEN + ROOK
        const val NON_PAWN_MATERIAL_MIDGAME_MAX = 3 * KNIGHT + 3 * BISHOP + 4 * ROOK + 2 * QUEEN

        /**
         * Merges two short Opening - Ending values in one int
         */
        fun oe(opening: Int, endgame: Int): Int {
            return (opening shl 16) + endgame
        }

        /**
         * Get the "Opening" part
         */
        fun o(oe: Int): Int {
            return oe + 0x8000 shr 16
        }

        /**
         * Get the "Endgame" part
         */
        fun e(oe: Int): Int {
            return (oe and 0xffff).toShort().toInt()
        }

        /**
         * Shift right each part by factor positions
         */
        fun oeShr(factor: Int, oeValue: Int): Int {
            return oe(o(oeValue) shr factor, e(oeValue) shr factor)
        }
    }
}