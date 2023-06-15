package com.ninidze.chesscomposekmm.domain.engine

import com.ninidze.chesscomposekmm.domain.engine.book.NoBook

/**
 * Holds configuration parameters

 * @author rui
 */
class Config {
    var transpositionTableSize = DEFAULT_TRANSPOSITION_TABLE_SIZE
    var useBook = DEFAULT_USE_BOOK
    var book = NoBook()
    var evaluator = DEFAULT_EVALUATOR
    var contemptFactor = DEFAULT_CONTEMPT_FACTOR

    var isUciChess960 = DEFAULT_UCI_CHESS960

    var rand = DEFAULT_RAND
    var bookKnowledge = DEFAULT_BOOK_KNOWGLEDGE
    var isLimitStrength = DEFAULT_LIMIT_STRENGTH
        set(limitStrength) {
            field = limitStrength
            calculateErrorsFromElo()
        }
    var elo = DEFAULT_ELO
        set(elo) {
            field = elo
            calculateErrorsFromElo()
        }

    /**
     * Calculates the errors and the book knowledge using the limitStrength and elo params
     * 2100 is the max elo, 500 the min
     */
    private fun calculateErrorsFromElo() {
        if (isLimitStrength) {
            rand = 900 - (this.elo - 500) * 900 / 1600 // Errors per 1000
            bookKnowledge = (this.elo - 500) * 100 / 1600 // In percentage
        } else {
            rand = 0
            bookKnowledge = 100
        }
    }

    companion object {
        // Default values are static fields used also from UCIEngine
        const val DEFAULT_TRANSPOSITION_TABLE_SIZE = 64
        const val DEFAULT_USE_BOOK = true
        const val DEFAULT_BOOK_KNOWGLEDGE = 100
        const val DEFAULT_EVALUATOR = "complete"

        // >0 refuses draw <0 looks for draw
        const val DEFAULT_CONTEMPT_FACTOR = 100
        const val DEFAULT_UCI_CHESS960 = false

        const val DEFAULT_RAND = 0
        const val DEFAULT_LIMIT_STRENGTH = false
        const val DEFAULT_ELO = 2100
    }
}