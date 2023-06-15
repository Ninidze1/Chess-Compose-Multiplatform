package com.ninidze.chesscomposekmm.domain.engine.log

class Logger private constructor(private var prefix: String) {

    fun debug(`in`: Any) {
        if (noLog) return
        print("DEBUG ")
        print(prefix)
        print(" - ")
        println(`in`.toString())
    }

    companion object {
        const val noLog = false

        fun getLogger(prefix: String): Logger {
            return Logger(prefix)
        }
    }
}
