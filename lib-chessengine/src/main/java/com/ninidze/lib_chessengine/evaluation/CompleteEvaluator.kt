package com.ninidze.chesscomposekmm.data.engine.evaluation

import com.ninidze.chesscomposekmm.domain.engine.Board
import com.ninidze.chesscomposekmm.domain.engine.Piece
import com.ninidze.chesscomposekmm.domain.engine.Square
import com.ninidze.chesscomposekmm.domain.engine.bitboard.AttacksInfo
import com.ninidze.chesscomposekmm.domain.engine.bitboard.BitboardUtils
import com.ninidze.chesscomposekmm.domain.engine.log.Logger

class CompleteEvaluator : Evaluator() {
    private val logger = Logger.getLogger("CompleteEvaluator")

    // Mobility units: this value is added for the number of destination square not occupied by one of our pieces or attacked by opposite pawns
    private val MOBILITY = arrayOf(intArrayOf(), intArrayOf(), intArrayOf(oe(-12, -16), oe(2, 2), oe(5, 7), oe(7, 9), oe(8, 11), oe(10, 13), oe(11, 14), oe(11, 15), oe(12, 16)), intArrayOf(
        oe(-16, -16), oe(-1, -1), oe(3, 3), oe(6, 6), oe(8, 8), oe(9, 9), oe(11, 11), oe(12, 12), oe(13, 13), oe(13, 13), oe(14, 14), oe(15, 15), oe(15, 15), oe(16, 16)
    ), intArrayOf(oe(-14, -21), oe(-1, -2), oe(3, 4), oe(5, 7), oe(7, 10), oe(8, 12), oe(9, 13), oe(10, 15), oe(11, 16), oe(11, 17), oe(12, 18), oe(13, 19), oe(13, 20), oe(14, 20), oe(14, 21)), intArrayOf(
        oe(-27, -27), oe(-9, -9), oe(-2, -2), oe(2, 2), oe(5, 5), oe(8, 8), oe(10, 10), oe(12, 12), oe(13, 13), oe(14, 14), oe(16, 16), oe(17, 17), oe(18, 18), oe(19, 19), oe(19, 19), oe(20, 20), oe(21, 21), oe(22, 22), oe(22, 22), oe(23, 23), oe(24, 24), oe(24, 24), oe(25, 25), oe(25, 25), oe(26, 26), oe(26, 26), oe(27, 27), oe(27, 27)
    ))

    // Space
    private val WHITE_SPACE_ZONE = BitboardUtils.C or BitboardUtils.D or BitboardUtils.E or BitboardUtils.F and (BitboardUtils.R2 or BitboardUtils.R3 or BitboardUtils.R4)
    private val BLACK_SPACE_ZONE = BitboardUtils.C or BitboardUtils.D or BitboardUtils.E or BitboardUtils.F and (BitboardUtils.R5 or BitboardUtils.R6 or BitboardUtils.R7)
    private val SPACE = oe(2, 0)

    // Attacks
    private val PAWN_ATTACKS = intArrayOf(0, 0, oe(11, 15), oe(12, 16), oe(17, 23), oe(19, 25), 0)
    private val MINOR_ATTACKS = intArrayOf(0, oe(3, 5), oe(7, 9), oe(7, 9), oe(10, 14), oe(11, 15), 0) // Minor piece attacks to pawn undefended pieces
    private val MAJOR_ATTACKS = intArrayOf(0, oe(2, 2), oe(3, 4), oe(3, 4), oe(5, 6), oe(5, 7), 0) // Major piece attacks to pawn undefended pieces

    private val HUNG_PIECES = oe(16, 25) // Two or more pieces of the other side attacked by inferior pieces
    private val PINNED_PIECE = oe(7, 15)

    // Pawns
    // Those are all penalties. Array is {not opposed, opposed}: If not opposed, backwards and isolated pawns can be easily attacked
    private val PAWN_BACKWARDS = intArrayOf(oe(20, 15), oe(10, 15)) // Not opposed is worse in the opening
    private val PAWN_ISOLATED = intArrayOf(oe(20, 20), oe(10, 20)) // Not opposed is worse in the opening
    private val PAWN_DOUBLED = intArrayOf(oe(8, 16), oe(10, 20)) // Not opposed is better, opening is better
    private val PAWN_UNSUPPORTED = oe(2, 4) // Not backwards or isolated

    // And now the bonuses. Array by relative rank
    private val PAWN_CANDIDATE = intArrayOf(0, oe(10, 13), oe(10, 13), oe(14, 18), oe(22, 28), oe(34, 43), oe(50, 63), 0)
    private val PAWN_PASSER = intArrayOf(0, oe(20, 25), oe(20, 25), oe(28, 35), oe(44, 55), oe(68, 85), oe(100, 125), 0)
    private val PAWN_PASSER_OUTSIDE = intArrayOf(0, 0, 0, oe(2, 3), oe(7, 9), oe(14, 18), oe(24, 30), 0)
    private val PAWN_PASSER_CONNECTED = intArrayOf(0, 0, 0, oe(3, 3), oe(8, 8), oe(15, 15), oe(25, 25), 0)
    private val PAWN_PASSER_SUPPORTED = intArrayOf(0, 0, 0, oe(6, 6), oe(17, 17), oe(33, 33), oe(55, 55), 0)
    private val PAWN_PASSER_MOBILE = intArrayOf(0, 0, 0, oe(2, 2), oe(6, 6), oe(12, 12), oe(20, 20), 0)
    private val PAWN_PASSER_RUNNER = intArrayOf(0, 0, 0, oe(6, 6), oe(18, 18), oe(36, 36), oe(60, 60), 0)

    private val PAWN_PASSER_OTHER_KING_DISTANCE = intArrayOf(0, 0, 0, oe(0, 2), oe(0, 6), oe(0, 12), oe(0, 20), 0)
    private val PAWN_PASSER_MY_KING_DISTANCE = intArrayOf(0, 0, 0, oe(0, 1), oe(0, 3), oe(0, 6), oe(0, 10), 0)

    private val PAWN_SHIELD_CENTER = intArrayOf(0, oe(55, 0), oe(41, 0), oe(28, 0), oe(14, 0), 0, 0, 0)
    private val PAWN_SHIELD = intArrayOf(0, oe(35, 0), oe(26, 0), oe(18, 0), oe(9, 0), 0, 0, 0)
    private val PAWN_STORM_CENTER = intArrayOf(0, 0, 0, oe(8, 0), oe(15, 0), oe(30, 0), 0, 0)
    private val PAWN_STORM = intArrayOf(0, 0, 0, oe(5, 0), oe(10, 0), oe(20, 0), 0, 0)

    private val PAWN_BLOCKADE = oe(5, 0) // Penalty for pawns in [D,E] in the initial square blocked by our own pieces

    // Knights
    private val KNIGHT_OUTPOST = intArrayOf(oe(15, 10), oe(22, 15)) // Array is Not defended by pawn, defended by pawn

    // Bishops
    private val BISHOP_OUTPOST = intArrayOf(oe(7, 4), oe(10, 7))
    private val BISHOP_MY_PAWNS_IN_COLOR_PENALTY = oe(2, 4) // Penalty for each of my pawns in the bishop color (Capablanca rule)
    private val BISHOP_TRAPPED_PENALTY = intArrayOf(oe(40, 40), oe(80, 80)) // By pawn not guarded / guarded
    private val BISHOP_TRAPPING = longArrayOf(// Indexed by bishop position, contains the square where a pawn can trap the bishop
            0, Square.F2, 0, 0, 0, 0, Square.C2, 0, Square.G3, 0, 0, 0, 0, 0, 0, Square.B3, Square.G4, 0, 0, 0, 0, 0, 0, Square.B4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Square.G5, 0, 0, 0, 0, 0, 0, Square.B5, Square.G6, 0, 0, 0, 0, 0, 0, Square.B6, 0, Square.F7, 0, 0, 0, 0, Square.C7, 0)
    private val BISHOP_TRAPPING_GUARD = longArrayOf(// Indexed by bishop position, contains the square where other pawn defends the trapping pawn
            0, 0, 0, 0, 0, 0, 0, 0, Square.F2, 0, 0, 0, 0, 0, 0, Square.C2, Square.F3, 0, 0, 0, 0, 0, 0, Square.C3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Square.F6, 0, 0, 0, 0, 0, 0, Square.C6, Square.F7, 0, 0, 0, 0, 0, 0, Square.C7, 0, 0, 0, 0, 0, 0, 0, 0)

    // Rooks
    private val ROOK_OUTPOST = intArrayOf(oe(2, 1), oe(3, 2)) // Array is Not defended by pawn, defended by pawn
    private val ROOK_FILE = intArrayOf(oe(15, 10), oe(7, 5)) // Open / Semi open
    private val ROOK_7 = oe(7, 10) // Rook 5, 6 or 7th rank attacking a pawn in the same rank not defended by pawn
    private val ROOK_TRAPPED_PENALTY = intArrayOf(oe(40, 0), oe(30, 0), oe(20, 0), oe(10, 0)) // Penalty by number of mobility squares
    private val ROOK_TRAPPING = longArrayOf(// Indexed by own king position, contains the squares where a rook may be traped by the king
            0, Square.H1 or Square.H2, Square.H1 or Square.H2 or Square.G1 or Square.G2, 0, 0, Square.A1 or Square.A2 or Square.B1 or Square.B2, Square.A1 or Square.A2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Square.H7 or Square.H8, Square.H7 or Square.H8 or Square.G7 or Square.G8, 0, 0, Square.A7 or Square.A8 or Square.B7 or Square.B8, Square.A7 or Square.A8, 0)

    // King
    // Sums for each piece attacking an square near the king
    private val PIECE_ATTACKS_KING = intArrayOf(0, 0, oe(30, 0), oe(20, 0), oe(40, 0), oe(80, 0))
    // Ponder kings attacks by the number of attackers (not pawns)
    private val KING_SAFETY_PONDER = intArrayOf(0, 0, 32, 48, 56, 60, 62, 63, 64, 64, 64, 64, 64, 64, 64, 64)


    private val OUTPOST_MASK = longArrayOf(0x00007e7e7e000000L, 0x0000007e7e7e0000L)

    private val pawnPcsq = intArrayOf(oe(-18, 4), oe(-6, 2), oe(0, 0), oe(6, -2), oe(6, -2), oe(0, 0), oe(-6, 2), oe(-18, 4), oe(-21, 1), oe(-9, -1), oe(-3, -3), oe(3, -5), oe(3, -5), oe(-3, -3), oe(-9, -1), oe(-21, 1), oe(-20, 1), oe(-8, -1), oe(-2, -3), oe(4, -5), oe(4, -5), oe(-2, -3), oe(-8, -1), oe(-20, 1), oe(-19, 2), oe(-7, 0), oe(-1, -2), oe(12, -4), oe(12, -4), oe(-1, -2), oe(-7, 0), oe(-19, 2), oe(-17, 3), oe(-5, 1), oe(1, -1), oe(10, -3), oe(10, -3), oe(1, -1), oe(-5, 1), oe(-17, 3), oe(-16, 4), oe(-4, 2), oe(2, 0), oe(8, -2), oe(8, -2), oe(2, 0), oe(-4, 2), oe(-16, 4), oe(-15, 6), oe(-3, 4), oe(3, 2), oe(9, 0), oe(9, 0), oe(3, 2), oe(-3, 4), oe(-15, 6), oe(-18, 4), oe(-6, 2), oe(0, 0), oe(6, -2), oe(6, -2), oe(0, 0), oe(-6, 2), oe(-18, 4))
    private val knightPcsq = intArrayOf(oe(-27, -22), oe(-17, -17), oe(-9, -12), oe(-4, -9), oe(-4, -9), oe(-9, -12), oe(-17, -17), oe(-27, -22), oe(-21, -15), oe(-11, -8), oe(-3, -4), oe(2, -2), oe(2, -2), oe(-3, -4), oe(-11, -8), oe(-21, -15), oe(-15, -10), oe(-5, -4), oe(3, 1), oe(8, 3), oe(8, 3), oe(3, 1), oe(-5, -4), oe(-15, -10), oe(-9, -6), oe(1, -1), oe(9, 4), oe(14, 8), oe(14, 8), oe(9, 4), oe(1, -1), oe(-9, -6), oe(-5, -4), oe(5, 1), oe(13, 6), oe(18, 10), oe(18, 10), oe(13, 6), oe(5, 1), oe(-5, -4), oe(-6, -4), oe(4, 2), oe(12, 7), oe(17, 9), oe(17, 9), oe(12, 7), oe(4, 2), oe(-6, -4), oe(-10, -8), oe(0, -1), oe(8, 3), oe(13, 5), oe(13, 5), oe(8, 3), oe(0, -1), oe(-10, -8), oe(-20, -15), oe(-10, -10), oe(-2, -5), oe(3, -2), oe(3, -2), oe(-2, -5), oe(-10, -10), oe(-20, -15))
    private val bishopPcsq = intArrayOf(oe(-7, 0), oe(-8, -1), oe(-11, -2), oe(-13, -2), oe(-13, -2), oe(-11, -2), oe(-8, -1), oe(-7, 0), oe(-3, -1), oe(3, 1), oe(0, 0), oe(-2, 0), oe(-2, 0), oe(0, 0), oe(3, 1), oe(-3, -1), oe(-6, -2), oe(0, 0), oe(7, 3), oe(6, 2), oe(6, 2), oe(7, 3), oe(0, 0), oe(-6, -2), oe(-8, -2), oe(-2, 0), oe(6, 2), oe(15, 5), oe(15, 5), oe(6, 2), oe(-2, 0), oe(-8, -2), oe(-8, -2), oe(-2, 0), oe(6, 2), oe(15, 5), oe(15, 5), oe(6, 2), oe(-2, 0), oe(-8, -2), oe(-6, -2), oe(0, 0), oe(7, 3), oe(6, 2), oe(6, 2), oe(7, 3), oe(0, 0), oe(-6, -2), oe(-3, -1), oe(3, 1), oe(0, 0), oe(-2, 0), oe(-2, 0), oe(0, 0), oe(3, 1), oe(-3, -1), oe(-2, 0), oe(-3, -1), oe(-6, -2), oe(-8, -2), oe(-8, -2), oe(-6, -2), oe(-3, -1), oe(-2, 0))
    private val rookPcsq = intArrayOf(oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0), oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0), oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0), oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0), oe(-4, 1), oe(0, 1), oe(4, 1), oe(8, 1), oe(8, 1), oe(4, 1), oe(0, 1), oe(-4, 1), oe(-4, 3), oe(0, 3), oe(4, 3), oe(8, 3), oe(8, 3), oe(4, 3), oe(0, 3), oe(-4, 3), oe(-4, 5), oe(0, 5), oe(4, 5), oe(8, 5), oe(8, 5), oe(4, 5), oe(0, 5), oe(-4, 5), oe(-4, -2), oe(0, -2), oe(4, -2), oe(8, -2), oe(8, -2), oe(4, -2), oe(0, -2), oe(-4, -2))
    private val queenPcsq = intArrayOf(oe(-9, -15), oe(-6, -10), oe(-4, -8), oe(-2, -7), oe(-2, -7), oe(-4, -8), oe(-6, -10), oe(-9, -15), oe(-6, -10), oe(-1, -5), oe(1, -3), oe(3, -2), oe(3, -2), oe(1, -3), oe(-1, -5), oe(-6, -10), oe(-4, -8), oe(1, -3), oe(5, 0), oe(6, 2), oe(6, 2), oe(5, 0), oe(1, -3), oe(-4, -8), oe(-2, -7), oe(3, -2), oe(6, 2), oe(9, 5), oe(9, 5), oe(6, 2), oe(3, -2), oe(-2, -7), oe(-2, -7), oe(3, -2), oe(6, 2), oe(9, 5), oe(9, 5), oe(6, 2), oe(3, -2), oe(-2, -7), oe(-4, -8), oe(1, -3), oe(5, 0), oe(6, 2), oe(6, 2), oe(5, 0), oe(1, -3), oe(-4, -8), oe(-6, -10), oe(-1, -5), oe(1, -3), oe(3, -2), oe(3, -2), oe(1, -3), oe(-1, -5), oe(-6, -10), oe(-9, -15), oe(-6, -10), oe(-4, -8), oe(-2, -7), oe(-2, -7), oe(-4, -8), oe(-6, -10), oe(-9, -15))
    private val kingPcsq = intArrayOf(oe(34, -58), oe(39, -35), oe(14, -19), oe(-6, -13), oe(-6, -13), oe(14, -19), oe(39, -35), oe(34, -58), oe(31, -35), oe(36, -10), oe(11, 2), oe(-9, 8), oe(-9, 8), oe(11, 2), oe(36, -10), oe(31, -35), oe(28, -19), oe(33, 2), oe(8, 17), oe(-12, 23), oe(-12, 23), oe(8, 17), oe(33, 2), oe(28, -19), oe(25, -13), oe(30, 8), oe(5, 23), oe(-15, 32), oe(-15, 32), oe(5, 23), oe(30, 8), oe(25, -13), oe(20, -13), oe(25, 8), oe(0, 23), oe(-20, 32), oe(-20, 32), oe(0, 23), oe(25, 8), oe(20, -13), oe(15, -19), oe(20, 2), oe(-5, 17), oe(-25, 23), oe(-25, 23), oe(-5, 17), oe(20, 2), oe(15, -19), oe(5, -35), oe(10, -10), oe(-15, 2), oe(-35, 8), oe(-35, 8), oe(-15, 2), oe(10, -10), oe(5, -35), oe(-5, -58), oe(0, -35), oe(-25, -19), oe(-45, -13), oe(-45, -13), oe(-25, -19), oe(0, -35), oe(-5, -58))

    var debug = false
    var debugPawns = false
    lateinit var debugSB: StringBuilder

    private val scaleFactor = intArrayOf(0)

    private val pawnMaterial = intArrayOf(0, 0)
    private val nonPawnMaterial = intArrayOf(0, 0)
    private val pcsq = intArrayOf(0, 0)
    private val space = intArrayOf(0, 0)
    private val positional = intArrayOf(0, 0)
    private val mobility = intArrayOf(0, 0)
    private val attacks = intArrayOf(0, 0)
    private val kingAttackersCount = intArrayOf(0, 0)
    private val kingSafety = intArrayOf(0, 0)
    private val pawnStructure = intArrayOf(0, 0)
    private val passedPawns = intArrayOf(0, 0)
    private val pawnCanAttack = longArrayOf(0, 0)
    private val mobilitySquares = longArrayOf(0, 0)
    private val kingZone = longArrayOf(0, 0) // Squares surrounding King

    override fun evaluate(b: Board, ai: AttacksInfo): Int {
        if (debug) {
            debugSB = StringBuilder()
            debugSB.append("\n")
            debugSB.append(b.toString())
            debugSB.append("\n")
        }

        val whitePawns = BitboardUtils.popCount(b.pawns and b.whites)
        val blackPawns = BitboardUtils.popCount(b.pawns and b.blacks)
        val whiteKnights = BitboardUtils.popCount(b.knights and b.whites)
        val blackKnights = BitboardUtils.popCount(b.knights and b.blacks)
        val whiteBishops = BitboardUtils.popCount(b.bishops and b.whites)
        val blackBishops = BitboardUtils.popCount(b.bishops and b.blacks)
        val whiteRooks = BitboardUtils.popCount(b.rooks and b.whites)
        val blackRooks = BitboardUtils.popCount(b.rooks and b.blacks)
        val whiteQueens = BitboardUtils.popCount(b.queens and b.whites)
        val blackQueens = BitboardUtils.popCount(b.queens and b.blacks)

        val endgameValue = Endgame.evaluateEndgame(
            b,
            scaleFactor,
            whitePawns,
            blackPawns,
            whiteKnights,
            blackKnights,
            whiteBishops,
            blackBishops,
            whiteRooks,
            blackRooks,
            whiteQueens,
            blackQueens
        )
        if (endgameValue != NO_VALUE) {
            return endgameValue
        }

        pawnMaterial[W] = whitePawns * PIECE_VALUES_OE[Piece.PAWN]
        nonPawnMaterial[W] = whiteKnights * PIECE_VALUES_OE[Piece.KNIGHT] +
                whiteBishops * PIECE_VALUES_OE[Piece.BISHOP] +
                whiteRooks * PIECE_VALUES_OE[Piece.ROOK] +
                whiteQueens * PIECE_VALUES_OE[Piece.QUEEN] +
                if (b.whites and b.bishops and Square.WHITES != 0L && b.whites and b.bishops and Square.BLACKS != 0L)
                    BISHOP_PAIR
                else
                    0
        pawnMaterial[B] = blackPawns * PIECE_VALUES_OE[Piece.PAWN]
        nonPawnMaterial[B] = blackKnights * PIECE_VALUES_OE[Piece.KNIGHT] +
                blackBishops * PIECE_VALUES_OE[Piece.BISHOP] +
                blackRooks * PIECE_VALUES_OE[Piece.ROOK] +
                blackQueens * PIECE_VALUES_OE[Piece.QUEEN] +
                if (b.blacks and b.bishops and Square.WHITES != 0L && b.blacks and b.bishops and Square.BLACKS != 0L)
                    BISHOP_PAIR
                else
                    0

        val nonPawnMat = e(nonPawnMaterial[W] + nonPawnMaterial[B])
        val gamePhase = if (nonPawnMat >= NON_PAWN_MATERIAL_MIDGAME_MAX)
            GAME_PHASE_MIDGAME
        else if (nonPawnMat <= NON_PAWN_MATERIAL_ENDGAME_MIN)
            GAME_PHASE_ENDGAME
        else
            (nonPawnMat - NON_PAWN_MATERIAL_ENDGAME_MIN) * GAME_PHASE_MIDGAME / (NON_PAWN_MATERIAL_MIDGAME_MAX - NON_PAWN_MATERIAL_ENDGAME_MIN)

        pcsq[W] = 0
        pcsq[B] = 0
        positional[W] = 0
        positional[B] = 0
        mobility[W] = 0
        mobility[B] = 0
        kingAttackersCount[W] = 0
        kingAttackersCount[B] = 0
        kingSafety[W] = 0
        kingSafety[B] = 0
        pawnStructure[W] = 0
        pawnStructure[B] = 0
        passedPawns[W] = 0
        passedPawns[B] = 0

        mobilitySquares[W] = b.whites.inv()
        mobilitySquares[B] = b.blacks.inv()

        ai.build(b)

        var whitePawnsAux = b.pawns and b.whites
        var blackPawnsAux = b.pawns and b.blacks

        // Space evaluation
        if (gamePhase > 0) {
            val whiteSafe = WHITE_SPACE_ZONE and ai.pawnAttacks[B].inv() and (ai.attackedSquares[B].inv() or ai.attackedSquares[W])
            val blackSafe = BLACK_SPACE_ZONE and ai.pawnAttacks[W].inv() and (ai.attackedSquares[W].inv() or ai.attackedSquares[B])

            val whiteBehindPawn = whitePawnsAux.ushr(8) or whitePawnsAux.ushr(16) or whitePawnsAux.ushr(24)
            val blackBehindPawn = blackPawnsAux shl 8 or (blackPawnsAux shl 16) or (blackPawnsAux shl 24)

            space[W] = SPACE * ((BitboardUtils.popCount(whiteSafe) + BitboardUtils.popCount(whiteSafe and whiteBehindPawn)) * (whiteKnights + whiteBishops) / 4)
            space[B] = SPACE * ((BitboardUtils.popCount(blackSafe) + BitboardUtils.popCount(blackSafe and blackBehindPawn)) * (blackKnights + blackBishops) / 4)
        } else {
            space[W] = 0
            space[B] = 0
        }

        // Squares that pawns attack or can attack by advancing
        pawnCanAttack[W] = ai.pawnAttacks[W]
        pawnCanAttack[B] = ai.pawnAttacks[B]
        for (i in 0..4) {
            whitePawnsAux = whitePawnsAux shl 8
            whitePawnsAux = whitePawnsAux and (b.pawns and b.blacks or ai.pawnAttacks[B]).inv() // Cannot advance because of a blocking pawn or a opposite pawn attack
            blackPawnsAux = blackPawnsAux.ushr(8)
            blackPawnsAux = blackPawnsAux and (b.pawns and b.whites or ai.pawnAttacks[W]).inv() // Cannot advance because of a blocking pawn or a opposite pawn attack

            if (whitePawnsAux == 0L && blackPawnsAux == 0L) {
                break
            }
            pawnCanAttack[W] = pawnCanAttack[W] or (whitePawnsAux and BitboardUtils.b_l.inv() shl 9 or (whitePawnsAux and BitboardUtils.b_r.inv() shl 7))
            pawnCanAttack[B] = pawnCanAttack[B] or ((blackPawnsAux and BitboardUtils.b_r.inv()).ushr(9) or (blackPawnsAux and BitboardUtils.b_l.inv()).ushr(7))
        }

        // Calculate attacks
        attacks[W] = evalAttacks(b, ai, W, b.blacks)
        attacks[B] = evalAttacks(b, ai, B, b.whites)

        // Squares surrounding King and three squares towards thew other side
        kingZone[W] = bbAttacks.king[ai.kingIndex[W]]
        kingZone[W] = kingZone[W] or (kingZone[W] shl 8)
        kingZone[B] = bbAttacks.king[ai.kingIndex[B]]
        kingZone[B] = kingZone[B] or kingZone[B].ushr(8)

        val all = b.all
        var pieceAttacks: Long
        var safeAttacks: Long
        var kingAttacks: Long

        var square: Long = 1

        for (index in 0..63) {
            if (square and all != 0L) {
                val isWhite = b.whites and square != 0L
                val us = if (isWhite) W else B
                val them = if (isWhite) B else W
                val mines = if (isWhite) b.whites else b.blacks
                val others = if (isWhite) b.blacks else b.whites
                val pcsqIndex = if (isWhite) index else 63 - index
                val rank = index shr 3
                val relativeRank = if (isWhite) rank else 7 - rank
                val file = 7 - index and 7

                pieceAttacks = ai.attacksFromSquare[index]

                if (square and b.pawns != 0L) {
                    pcsq[us] += pawnPcsq[pcsqIndex]

                    val myPawns = b.pawns and mines
                    val otherPawns = b.pawns and others
                    val adjacentFiles = BitboardUtils.FILES_ADJACENT[file]
                    val ranksForward = BitboardUtils.RANKS_FORWARD[us][rank]
                    val pawnFile = BitboardUtils.FILE[file]
                    val routeToPromotion = pawnFile and ranksForward
                    val otherPawnsAheadAdjacent = ranksForward and adjacentFiles and otherPawns
                    val pushSquare = if (isWhite) square shl 8 else square.ushr(8)

                    val supported = square and ai.pawnAttacks[us] != 0L
                    val doubled = myPawns and routeToPromotion != 0L
                    val opposed = otherPawns and routeToPromotion != 0L
                    val passed = !doubled
                            && !opposed
                            && otherPawnsAheadAdjacent == 0L

                    if (!passed) {
                        val myPawnsAheadAdjacent = ranksForward and adjacentFiles and myPawns
                        val myPawnsBesideAndBehindAdjacent = BitboardUtils.RANK_AND_BACKWARD[us][rank] and adjacentFiles and myPawns
                        val isolated = myPawns and adjacentFiles == 0L
                        val candidate = !doubled
                                && !opposed
                                && (otherPawnsAheadAdjacent and pieceAttacks.inv() == 0L || // Can become passer advancing
                                BitboardUtils.popCount(myPawnsBesideAndBehindAdjacent) >= BitboardUtils.popCount(otherPawnsAheadAdjacent and pieceAttacks.inv())) // Has more friend pawns beside and behind than opposed pawns controlling his route to promotion
                        val backward = !isolated
                                && !candidate
                                && myPawnsBesideAndBehindAdjacent == 0L
                                && pieceAttacks and otherPawns == 0L // No backwards if it can capture
                                && BitboardUtils.RANK_AND_BACKWARD[us][if (isWhite) BitboardUtils.getRankLsb(myPawnsAheadAdjacent) else BitboardUtils.getRankMsb(myPawnsAheadAdjacent)] and
                                routeToPromotion and (b.pawns or ai.pawnAttacks[them]) != 0L // Other pawns stopping it from advance, opposing or capturing it before reaching my pawns

                        if (debugPawns) {
                            val connected = bbAttacks.king[index] and adjacentFiles and myPawns != 0L
                            debugSB.append("PAWN " + BitboardUtils.SQUARE_NAMES[index] +
                                    (if (isWhite) " WHITE " else " BLACK ") +
                                    (if (isolated) "isolated " else "") +
                                    (if (supported) "supported " else "") +
                                    (if (connected) "connected " else "") +
                                    (if (doubled) "doubled " else "") +
                                    (if (opposed) "opposed " else "") +
                                    (if (candidate) "candidate " else "") +
                                    (if (backward) "backward " else "") +
                                    "\n"
                            )
                        }

                        if (backward) {
                            pawnStructure[us] -= PAWN_BACKWARDS[if (opposed) 1 else 0]
                        }
                        if (isolated) {
                            pawnStructure[us] -= PAWN_ISOLATED[if (opposed) 1 else 0]
                        }
                        if (doubled) {
                            pawnStructure[us] -= PAWN_DOUBLED[if (opposed) 1 else 0]
                        }
                        if (!supported
                                && !isolated
                                && !backward) {
                            pawnStructure[us] -= PAWN_UNSUPPORTED
                        }
                        if (candidate) {
                            passedPawns[us] += PAWN_CANDIDATE[relativeRank]
                        }

                        if (square and (BitboardUtils.D or BitboardUtils.E) != 0L
                                && relativeRank == 1
                                && pushSquare and mines and b.pawns.inv() != 0L) {
                            pawnStructure[us] -= PAWN_BLOCKADE
                        }

                        // Pawn Storm: It can open a file near the other king
                        if (gamePhase > 0 && relativeRank > 2) {
                            // Only if in kingside or queenside
                            val stormedPawns = otherPawnsAheadAdjacent and BitboardUtils.D.inv() and BitboardUtils.E.inv()
                            if (stormedPawns != 0L) {
                                // The stormed pawn must be in the other king's adjacent files
                                val otherKingFile = 7 - ai.kingIndex[them] and 7
                                if (stormedPawns and BitboardUtils.FILE[otherKingFile] != 0L) {
                                    pawnStructure[us] += PAWN_STORM_CENTER[relativeRank]
                                } else if (stormedPawns and BitboardUtils.FILES_ADJACENT[otherKingFile] != 0L) {
                                    pawnStructure[us] += PAWN_STORM[relativeRank]
                                }
                            }
                        }
                    } else {
                        //
                        // Passed Pawn
                        //
                        // Backfile only to the first piece found
                        val backFile = bbAttacks.getRookAttacks(index, all) and pawnFile and BitboardUtils.RANKS_BACKWARD[us][rank]
                        // If it has a rook or queen behind consider all the route to promotion attacked or defended
                        val attackedAndNotDefendedRoute =
                                ((routeToPromotion and ai.attackedSquares[them]) or if (backFile and (b.rooks or b.queens) and others != 0L) routeToPromotion else 0L) and
                                        ((routeToPromotion and ai.attackedSquares[us]) or if (backFile and (b.rooks or b.queens) and mines != 0L) routeToPromotion else 0L).inv()
                        val connected = bbAttacks.king[index] and adjacentFiles and myPawns != 0L
                        val outside = otherPawns != 0L && (square and BitboardUtils.FILES_LEFT[3] != 0L && b.pawns and BitboardUtils.FILES_LEFT[file] == 0L || square and BitboardUtils.FILES_RIGHT[4] != 0L && b.pawns and BitboardUtils.FILES_RIGHT[file] == 0L)
                        val mobile = pushSquare and (all or attackedAndNotDefendedRoute) == 0L
                        val runner = mobile
                                && routeToPromotion and all == 0L
                                && attackedAndNotDefendedRoute == 0L

                        if (debug) {
                            debugSB.append("PAWN " + BitboardUtils.SQUARE_NAMES[index] +
                                    (if (isWhite) " WHITE " else " BLACK ") +
                                    "passed " +
                                    (if (outside) "outside " else "") +
                                    (if (connected) "connected " else "") +
                                    (if (supported) "supported " else "") +
                                    (if (mobile) "mobile " else "") +
                                    (if (runner) "runner " else "") +
                                    "\n"
                            )
                        }

                        passedPawns[us] += PAWN_PASSER[relativeRank]

                        if (relativeRank >= 2) {
                            val pushIndex = if (isWhite) index + 8 else index - 8
                            passedPawns[us] += BitboardUtils.distance(pushIndex, ai.kingIndex[them]) * PAWN_PASSER_OTHER_KING_DISTANCE[relativeRank] - BitboardUtils.distance(pushIndex, ai.kingIndex[us]) * PAWN_PASSER_MY_KING_DISTANCE[relativeRank]
                        }
                        if (outside) {
                            passedPawns[us] += PAWN_PASSER_OUTSIDE[relativeRank]
                        }
                        if (supported) {
                            passedPawns[us] += PAWN_PASSER_SUPPORTED[relativeRank]
                        } else if (connected) {
                            passedPawns[us] += PAWN_PASSER_CONNECTED[relativeRank]
                        }
                        if (runner) {
                            passedPawns[us] += PAWN_PASSER_RUNNER[relativeRank]
                        } else if (mobile) {
                            passedPawns[us] += PAWN_PASSER_MOBILE[relativeRank]
                        }
                    }
                    // Pawn is part of the king shield
                    if (gamePhase > 0 && pawnFile and ranksForward.inv() and kingZone[us] and BitboardUtils.D.inv() and BitboardUtils.E.inv() != 0L) { // Pawn in the kingzone
                        pawnStructure[us] += if (pawnFile and b.kings and mines != 0L)
                            PAWN_SHIELD_CENTER[relativeRank]
                        else
                            PAWN_SHIELD[relativeRank]
                    }

                } else if (square and b.knights != 0L) {
                    pcsq[us] += knightPcsq[pcsqIndex]

                    safeAttacks = pieceAttacks and ai.pawnAttacks[them].inv()

                    mobility[us] += MOBILITY[Piece.KNIGHT][BitboardUtils.popCount(safeAttacks and mobilitySquares[us])]

                    kingAttacks = safeAttacks and kingZone[them]
                    if (kingAttacks != 0L) {
                        kingSafety[us] += PIECE_ATTACKS_KING[Piece.KNIGHT] * BitboardUtils.popCount(kingAttacks)
                        kingAttackersCount[us]++
                    }

                    // Knight outpost: no opposite pawns can attack the square
                    if (square and OUTPOST_MASK[us] and pawnCanAttack[them].inv() != 0L) {
                        positional[us] += KNIGHT_OUTPOST[if (square and ai.pawnAttacks[us] != 0L) 1 else 0]
                    }

                } else if (square and b.bishops != 0L) {
                    pcsq[us] += bishopPcsq[pcsqIndex]

                    safeAttacks = pieceAttacks and ai.pawnAttacks[them].inv()

                    mobility[us] += MOBILITY[Piece.BISHOP][BitboardUtils.popCount(safeAttacks and mobilitySquares[us])]

                    kingAttacks = safeAttacks and kingZone[them]
                    if (kingAttacks != 0L) {
                        kingSafety[us] += PIECE_ATTACKS_KING[Piece.BISHOP] * BitboardUtils.popCount(kingAttacks)
                        kingAttackersCount[us]++
                    }

                    // Bishop Outpost
                    if (square and OUTPOST_MASK[us] and pawnCanAttack[them].inv() != 0L) {
                        positional[us] += BISHOP_OUTPOST[if (square and ai.pawnAttacks[us] != 0L) 1 else 0]
                    }

                    positional[us] -= BISHOP_MY_PAWNS_IN_COLOR_PENALTY * BitboardUtils.popCount(b.pawns and mines and BitboardUtils.getSameColorSquares(square))

                    if (BISHOP_TRAPPING[index] and b.pawns and others != 0L) {
                        mobility[us] -= BISHOP_TRAPPED_PENALTY[if (BISHOP_TRAPPING_GUARD[index] and b.pawns and others != 0L) 1 else 0]
                    }

                } else if (square and b.rooks != 0L) {
                    pcsq[us] += rookPcsq[pcsqIndex]

                    safeAttacks = pieceAttacks and ai.pawnAttacks[them].inv() and ai.knightAttacks[them].inv() and ai.bishopAttacks[them].inv()

                    val mobilityCount = BitboardUtils.popCount(safeAttacks and mobilitySquares[us])
                    mobility[us] += MOBILITY[Piece.ROOK][mobilityCount]

                    kingAttacks = safeAttacks and kingZone[them]
                    if (kingAttacks != 0L) {
                        kingSafety[us] += PIECE_ATTACKS_KING[Piece.ROOK] * BitboardUtils.popCount(kingAttacks)
                        kingAttackersCount[us]++
                    }

                    if (square and OUTPOST_MASK[us] and pawnCanAttack[them].inv() != 0L) {
                        positional[us] += ROOK_OUTPOST[if (square and ai.pawnAttacks[us] != 0L) 1 else 0]
                    }

                    val rookFile = BitboardUtils.FILE[file]
                    if (rookFile and b.pawns and mines == 0L) {
                        positional[us] += ROOK_FILE[if (rookFile and b.pawns == 0L) 0 else 1]
                    }

                    if (relativeRank >= 4) {
                        val pawnsAligned = BitboardUtils.RANK[rank] and b.pawns and others
                        if (pawnsAligned != 0L) {
                            positional[us] += ROOK_7 * BitboardUtils.popCount(pawnsAligned)
                        }
                    }

                    // Rook trapped by king
                    if (square and ROOK_TRAPPING[ai.kingIndex[us]] != 0L && mobilityCount < ROOK_TRAPPED_PENALTY.size) {
                        positional[us] -= ROOK_TRAPPED_PENALTY[mobilityCount]
                    }

                } else if (square and b.queens != 0L) {
                    pcsq[us] += queenPcsq[pcsqIndex]

                    safeAttacks = pieceAttacks and ai.pawnAttacks[them].inv() and ai.knightAttacks[them].inv() and ai.bishopAttacks[them].inv() and ai.rookAttacks[them].inv()

                    mobility[us] += MOBILITY[Piece.QUEEN][BitboardUtils.popCount(safeAttacks and mobilitySquares[us])]

                    kingAttacks = safeAttacks and kingZone[them]
                    if (kingAttacks != 0L) {
                        kingSafety[us] += PIECE_ATTACKS_KING[Piece.QUEEN] * BitboardUtils.popCount(kingAttacks)
                        kingAttackersCount[us]++
                    }

                } else if (square and b.kings != 0L) {
                    pcsq[us] += kingPcsq[pcsqIndex]
                }
            }
            square = square shl 1
        }

        val oe = (if (b.turn) TEMPO else -TEMPO) + pawnMaterial[W] - pawnMaterial[B] + nonPawnMaterial[W] - nonPawnMaterial[B] + pcsq[W] - pcsq[B] + space[W] - space[B] + positional[W] - positional[B] + attacks[W] - attacks[B] + mobility[W] - mobility[B] + pawnStructure[W] - pawnStructure[B] + passedPawns[W] - passedPawns[B] + oeShr(6, KING_SAFETY_PONDER[kingAttackersCount[W]] * kingSafety[W] - KING_SAFETY_PONDER[kingAttackersCount[B]] * kingSafety[B])

        // Ponder opening and Endgame value depending of the game phase and the scale factor
        val value = (gamePhase * o(oe) + (GAME_PHASE_MIDGAME - gamePhase) * e(oe) * scaleFactor[0] / Endgame.SCALE_FACTOR_DEFAULT) / GAME_PHASE_MIDGAME

        if (debug) {
            logger.debug(debugSB)
            logger.debug("                    WOpening WEndgame BOpening BEndgame")
            logger.debug("pawnMaterial      = " + formatOE(pawnMaterial[W]) + " " + formatOE(pawnMaterial[B]))
            logger.debug("nonPawnMaterial   = " + formatOE(nonPawnMaterial[W]) + " " + formatOE(nonPawnMaterial[B]))
            logger.debug("pcsq              = " + formatOE(pcsq[W]) + " " + formatOE(pcsq[B]))
            logger.debug("space             = " + formatOE(space[W]) + " " + formatOE(space[B]))
            logger.debug("mobility          = " + formatOE(mobility[W]) + " " + formatOE(mobility[B]))
            logger.debug("positional        = " + formatOE(positional[W]) + " " + formatOE(positional[B]))
            logger.debug("pawnStructure     = " + formatOE(pawnStructure[W]) + " " + formatOE(pawnStructure[B]))
            logger.debug("passedPawns       = " + formatOE(passedPawns[W]) + " " + formatOE(passedPawns[B]))
            logger.debug("attacks           = " + formatOE(attacks[W]) + " " + formatOE(attacks[B]))
            logger.debug("kingSafety        = " + formatOE(oeShr(6, KING_SAFETY_PONDER[kingAttackersCount[W]] * kingSafety[W])) + " " + formatOE(
                oeShr(6, KING_SAFETY_PONDER[kingAttackersCount[B]] * kingSafety[B])
            ))
            logger.debug("tempo             = " + formatOE(if (b.turn) TEMPO else -TEMPO))
            logger.debug("                    -----------------")
            logger.debug("TOTAL:              " + formatOE(oe))
            logger.debug("gamePhase = $gamePhase => value = $value")
        }
        //assert(Math.abs(value) < Evaluator.Companion.KNOWN_WIN) { "Eval is outside limits" }
        return value
    }

    private fun evalAttacks(board: Board, ai: AttacksInfo, us: Int, others: Long): Int {
        var attacks = 0

        var attackedByPawn = ai.pawnAttacks[us] and others and board.pawns.inv()
        while (attackedByPawn != 0L) {
            val lsb = BitboardUtils.lsb(attackedByPawn)
            attacks += PAWN_ATTACKS[board.getPieceIntAt(lsb)]
            attackedByPawn = attackedByPawn and lsb.inv()
        }

        val otherWeak = ai.attackedSquares[us] and others and ai.pawnAttacks[1 - us].inv()
        if (otherWeak != 0L) {
            var attackedByMinor = ai.knightAttacks[us] or ai.bishopAttacks[us] and otherWeak
            while (attackedByMinor != 0L) {
                val lsb = BitboardUtils.lsb(attackedByMinor)
                attacks += MINOR_ATTACKS[board.getPieceIntAt(lsb)]
                attackedByMinor = attackedByMinor and lsb.inv()
            }
            var attackedByMajor = ai.rookAttacks[us] or ai.queenAttacks[us] and otherWeak
            while (attackedByMajor != 0L) {
                val lsb = BitboardUtils.lsb(attackedByMajor)
                attacks += MAJOR_ATTACKS[board.getPieceIntAt(lsb)]
                attackedByMajor = attackedByMajor and lsb.inv()
            }
        }

        val superiorAttacks = ai.pawnAttacks[us] and others and board.pawns.inv() or (ai.knightAttacks[us] or ai.bishopAttacks[us] and others and (board.rooks or board.queens)) or (ai.rookAttacks[us] and others and board.queens)
        val superiorAttacksCount = BitboardUtils.popCount(superiorAttacks)
        if (superiorAttacksCount >= 2) {
            attacks += superiorAttacksCount * HUNG_PIECES
        }

        val pinnedNotPawn = ai.pinnedPieces and board.pawns.inv() and others
        if (pinnedNotPawn != 0L) {
            attacks += PINNED_PIECE * BitboardUtils.popCount(pinnedNotPawn)
        }
        return attacks
    }

    companion object {
        // Tempo
        val TEMPO = oe(15, 5) // Add to moving side score

    }
}